import { error } from '@sveltejs/kit';

import { downloadFile } from '#lib/api/index.js';
import { status } from '#lib/http.js';
import { apiClient, call } from '#lib/server/api.js';

import type { RequestHandler } from './$types';

// Resuming and revalidating go through to the api; it answers with 206 / 304 itself.
const FORWARDED_REQUEST_HEADERS = ['range', 'if-range', 'if-none-match', 'if-modified-since'];

// Including the api's download protections (attachment, nosniff, sandbox), which must reach the browser.
const FORWARDED_RESPONSE_HEADERS = [
  'content-type',
  'content-length',
  'content-range',
  'accept-ranges',
  'content-disposition',
  'etag',
  'last-modified',
  'cache-control',
  'x-content-type-options',
  'content-security-policy',
];

/**
 * Streams a file from the api with the session's token, since the browser has none. Visibility is checked by the api:
 * files of hidden challenges are a 404 unless you're an admin.
 */
export const GET: RequestHandler = async (event) => {
  const id = Number(event.params.id);
  if (!Number.isInteger(id)) error(status.NOT_FOUND, 'File not found');

  const headers = Object.fromEntries(
    FORWARDED_REQUEST_HEADERS.flatMap((name) => {
      const value = event.request.headers.get(name);
      return value ? [[name, value]] : [];
    }),
  );

  // The body is streamed through as is, so only the response is needed (parseAs keeps the client from reading it).
  const { response } = await call(
    downloadFile({ client: apiClient(event), path: { id: String(id) }, headers, parseAs: 'stream' }),
  );
  if (response.status === status.NOT_MODIFIED)
    return new Response(null, { status: status.NOT_MODIFIED, headers: pick(response.headers) });
  if (!response.ok)
    error(response.status, response.status === status.NOT_FOUND ? 'File not found' : response.statusText);

  return new Response(response.body, { status: response.status, headers: pick(response.headers) });
};

function pick(source: Headers) {
  const headers = new Headers();
  for (const name of FORWARDED_RESPONSE_HEADERS) {
    const value = source.get(name);
    if (value) headers.set(name, value);
  }
  return headers;
}
