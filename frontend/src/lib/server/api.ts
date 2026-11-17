import { API_URL } from '$app/env/private';
import type { RequestEvent } from '@sveltejs/kit';

import { createClient, createConfig } from '#lib/api/client/index.js';
import { status } from '#lib/http.js';

import { deleteSession } from './session';
import { injectTraceContext } from './trace';

/**
 * Adds the session's JWT, the browser's IP (for the api's rate limiting) and user agent (kept with submissions), and
 * the trace context, so the api's spans join this request's trace.
 */
function authorize(event: RequestEvent, request: Request) {
  const token = event.locals.session?.token;
  request.headers.set('X-Forwarded-For', event.getClientAddress());
  const userAgent = event.request.headers.get('User-Agent');
  if (userAgent) request.headers.set('User-Agent', userAgent);
  if (token) request.headers.set('Authorization', `Bearer ${token}`);
  injectTraceContext(request.headers);
  return request;
}

/** A 401 on an authenticated call means the token is no longer valid, so the session is dropped. */
async function dropRejectedSession(event: RequestEvent, response: Response) {
  if (response.status === status.UNAUTHORIZED && event.locals.session) {
    await deleteSession(event.cookies);
    event.locals.session = undefined;
    event.locals.user = undefined;
  }
  return response;
}

/**
 * Server-side client for the api, authenticated as the session's user. Pass it to the generated endpoint functions
 * (`#lib/api`, from api/openapi.json by `bun run gen:api`), through {@link call}:
 *
 * ```ts
 * const client = apiClient(event);
 * const { data, error, response } = await call(getUser({ client, path: { id: '1' } }));
 * ```
 */
export function apiClient(event: RequestEvent) {
  const client = createClient(createConfig({ baseUrl: API_URL }));
  client.interceptors.request.use((request) => authorize(event, request));
  client.interceptors.response.use((response) => dropRejectedSession(event, response));
  return client;
}

/**
 * Awaits an endpoint call. The client reports a request that got no response (the api is down) as an error with no
 * response; that throws here instead, as fetch does, so every result has its response.
 */
export async function call<T extends { error?: unknown; response?: Response }>(
  request: Promise<T>,
): Promise<T & { response: Response }> {
  const result = await request;
  const { response } = result;
  if (!response) throw result.error;
  return { ...result, response };
}

/**
 * Untyped call to the api, authenticated like {@link apiClient}, for what the generated functions can't express, such
 * as streaming a request body through.
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
