import { error, redirect } from '@sveltejs/kit';

import { exportMe } from '#lib/api/index.js';
import { status } from '#lib/http.js';
import { apiClient, call } from '#lib/server/api.js';
import { problemMessage } from '#lib/server/problem.js';

import type { RequestHandler } from './$types';

export const GET: RequestHandler = async (event) => {
  if (!event.locals.user) redirect(status.SEE_OTHER, '/login');

  const { data, error: apiError, response } = await call(exportMe({ client: apiClient(event) }));
  if (apiError) {
    if (response.status === status.UNAUTHORIZED) redirect(status.SEE_OTHER, '/login');
    error(response.status, problemMessage(apiError));
  }

  return new Response(JSON.stringify(data, null, 2), {
    headers: {
      'Content-Type': 'application/json',
      'Content-Disposition': 'attachment; filename="ctfp-account.json"',
      'Cache-Control': 'no-store',
    },
  });
};
