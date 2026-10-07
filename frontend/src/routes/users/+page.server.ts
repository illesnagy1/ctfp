import { error, redirect } from '@sveltejs/kit';

import { apiClient } from '#lib/server/api.js';

import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  if (!event.locals.user) redirect(303, '/login');

  const { data, error: apiError, response } = await apiClient(event).GET('/v1/users');
  if (apiError) {
    if (response.status === 401) redirect(303, '/login');
    error(response.status, apiError.message);
  }

  return { users: data };
};
