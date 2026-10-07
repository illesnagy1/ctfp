import { error, redirect } from '@sveltejs/kit';

import { apiClient } from '#lib/server/api.js';

import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  const id = Number(event.params.id);
  if (!Number.isInteger(id)) error(404, 'User not found');

  const {
    data,
    error: apiError,
    response,
  } = await apiClient(event).GET('/v1/admin/users/{id}', { params: { path: { id: String(id) } } });
  if (apiError) {
    if (response.status === 401) redirect(303, '/login');
    error(response.status, apiError.message);
  }

  return { profile: data };
};
