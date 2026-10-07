import { API_URL } from '$app/env/private';
import type { RequestEvent } from '@sveltejs/kit';
import createClient from 'openapi-fetch';

import type { paths } from '#lib/types/api.js';

import { deleteSession } from './session';

/**
 * Server-side client for the api, typed from api/openapi.json (`bun run gen:api`). Adds the session's JWT and the
 * browser's IP (for the api's rate limiting). A 401 on an authenticated call means the token is no longer valid, so
 * the session is dropped.
 *
 * ```ts
 * const { data, error, response } = await apiClient(event).GET('/v1/users/{id}', { params: { path: { id: '1' } } });
 * ```
 */
export function apiClient(event: RequestEvent) {
  const client = createClient<paths>({ baseUrl: API_URL });
  const token = event.locals.session?.token;

  client.use({
    onRequest({ request }) {
      request.headers.set('X-Forwarded-For', event.getClientAddress());
      if (token) request.headers.set('Authorization', `Bearer ${token}`);
      return request;
    },
    async onResponse({ response }) {
      if (response.status === 401 && token) {
        await deleteSession(event.cookies);
        event.locals.session = undefined;
        event.locals.user = undefined;
      }
      return response;
    },
  });

  return client;
}
