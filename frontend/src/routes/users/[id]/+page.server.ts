import { error, redirect } from '@sveltejs/kit';

import { apiClient } from '#lib/server/api.js';

import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  const { user } = event.locals;
  if (!user) redirect(303, '/login');

  const id = Number(event.params.id);
  if (!Number.isInteger(id)) error(404, 'User not found');

  // Your own account comes with its full details; anyone else's is the public view.
  const client = apiClient(event);
  const {
    data,
    error: apiError,
    response,
  } = id === user.id
    ? await client.GET('/v1/users/me')
    : await client.GET('/v1/users/{id}', { params: { path: { id: String(id) } } });
  if (apiError) {
    if (response.status === 401) redirect(303, '/login');
    error(response.status, apiError.message);
  }

  return { profile: data };
};
