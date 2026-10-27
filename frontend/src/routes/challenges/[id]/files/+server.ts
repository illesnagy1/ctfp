import { error } from '@sveltejs/kit';

import { apiFetch } from '#lib/server/api.js';

import type { RequestHandler } from './$types';

/**
 * Passes a multipart upload (one `file` part) through to the api without parsing it, so large files are never held in
 * memory. Not a form action: those read the whole body as FormData. Needs BODY_SIZE_LIMIT raised (see compose.yml).
 * Responds with the api's FileResponse.
 */
export const POST: RequestHandler = async (event) => {
  // Endpoints don't run the layout's guards; the api checks the role again either way.
  if (event.locals.user?.role !== 'ADMIN') error(403, 'Admin access required');

  const id = Number(event.params.id);
  if (!Number.isInteger(id)) error(404, 'Challenge not found');

  const contentType = event.request.headers.get('content-type');
  if (!contentType?.startsWith('multipart/form-data') || !event.request.body) error(400, 'Expected a multipart upload');

  // Raw fetch: the typed client can't take a stream as the body (the spec has no multipart schema).
  const response = await apiFetch(event, `/v1/challenges/${id}/files`, {
    method: 'POST',
    // The browser's stream, boundary and all.
    body: event.request.body,
    headers: { 'Content-Type': contentType },
    // Required by fetch to send a stream as the body.
    duplex: 'half',
  });
  const result: unknown = await response.json().catch(() => undefined);

  if (!response.ok) {
    const message =
      typeof result === 'object' && result !== null && 'message' in result && typeof result.message === 'string'
        ? result.message
        : response.statusText;
    error(response.status, message);
  }
  return Response.json(result, { status: 201 });
};
