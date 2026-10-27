import { API_URL } from '$app/env/private';
import type { RequestEvent } from '@sveltejs/kit';
import createClient from 'openapi-fetch';

import type { paths } from '#lib/types/api.js';

import { deleteSession } from './session';

/** Adds the session's JWT and the browser's IP (for the api's rate limiting). */
function authorize(event: RequestEvent, request: Request) {
  const token = event.locals.session?.token;
  request.headers.set('X-Forwarded-For', event.getClientAddress());
  if (token) request.headers.set('Authorization', `Bearer ${token}`);
  return request;
}

/** A 401 on an authenticated call means the token is no longer valid, so the session is dropped. */
async function dropRejectedSession(event: RequestEvent, response: Response) {
  if (response.status === 401 && event.locals.session) {
    await deleteSession(event.cookies);
    event.locals.session = undefined;
    event.locals.user = undefined;
  }
  return response;
}

/**
 * Server-side client for the api, typed from api/openapi.json (`bun run gen:api`). Authenticated as the session's user.
 *
 * ```ts
 * const { data, error, response } = await apiClient(event).GET('/v1/users/{id}', { params: { path: { id: '1' } } });
 * ```
 */
export function apiClient(event: RequestEvent) {
  const client = createClient<paths>({ baseUrl: API_URL });

  client.use({
    onRequest: ({ request }) => authorize(event, request),
    onResponse: ({ response }) => dropRejectedSession(event, response),
  });

  return client;
}

/**
 * Untyped call to the api, authenticated like {@link apiClient}, for what the typed client can't express, such as
 * streaming a request body through.
 */
export async function apiFetch(
  event: RequestEvent,
  path: string,
  // `duplex: 'half'` is required to send a stream as the body, but missing from TypeScript's RequestInit.
  init: RequestInit & { duplex?: 'half' },
) {
  const response = await fetch(authorize(event, new Request(new URL(path, API_URL), init)));
  return dropRejectedSession(event, response);
}
