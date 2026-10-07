import { API_URL } from '$app/env/private';
import type { RequestEvent } from '@sveltejs/kit';

import { deleteSession } from './session';

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
  }
}

/**
 * Server-side call to the api. Adds the session's JWT and the browser's IP (for the api's rate limiting).
 * A 401 on an authenticated call means the token is no longer valid, so the session is dropped.
 */
export async function api<T>(event: RequestEvent, path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  headers.set('Accept', 'application/json');
  headers.set('X-Forwarded-For', event.getClientAddress());
  if (init.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');

  const token = event.locals.session?.token;
  if (token) headers.set('Authorization', `Bearer ${token}`);

  const response = await fetch(new URL(path, API_URL), { ...init, headers });

  if (!response.ok) {
    if (response.status === 401 && token) {
      await deleteSession(event.cookies);
      event.locals.session = undefined;
      event.locals.user = undefined;
    }
    const body = await response.json().catch(() => undefined);
    throw new ApiError(response.status, body?.message ?? response.statusText);
  }

  return (response.status === 204 ? undefined : await response.json()) as T;
}
