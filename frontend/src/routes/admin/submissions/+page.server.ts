import { error, redirect } from '@sveltejs/kit';

import { apiClient } from '#lib/server/api.js';

import type { PageServerLoad } from './$types';

/** Everyone's guesses; the admin layout already checked the role. */
export const load: PageServerLoad = async (event) => {
  const { data, error: apiError, response } = await apiClient(event).GET('/v1/admin/submissions');
  if (apiError) {
    if (response.status === 401) redirect(303, '/login');
    error(response.status, apiError.message);
  }
  return { submissions: data };
};
