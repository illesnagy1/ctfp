import { error, redirect } from '@sveltejs/kit';

import { listSubmissions } from '#lib/api/index.js';
import { status } from '#lib/http.js';
import { apiClient, call } from '#lib/server/api.js';

import type { PageServerLoad } from './$types';

/** Everyone's guesses; the admin layout already checked the role. */
export const load: PageServerLoad = async (event) => {
  const { data, error: apiError, response } = await call(listSubmissions({ client: apiClient(event) }));
  if (apiError) {
    if (response.status === status.UNAUTHORIZED) redirect(status.SEE_OTHER, '/login');
    error(response.status, apiError.message);
  }
  return { submissions: data };
};
