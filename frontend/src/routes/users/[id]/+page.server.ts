import { error, redirect } from '@sveltejs/kit';

import { apiClient } from '#lib/server/api.js';

import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  const { user } = event.locals;
  if (!user) redirect(303, '/login');

  const id = Number(event.params.id);
  if (!Number.isInteger(id)) error(404, 'User not found');

  // Admins see everyone's full details (banned and hidden users too), others their own; anyone else's is the public
  // view, which hides banned and hidden users.
  const client = apiClient(event);
  const params = { path: { id: String(id) } };
  const {
    data,
    error: apiError,
    response,
  } = user.role === 'ADMIN'
    ? await client.GET('/v1/admin/users/{id}', { params })
    : id === user.id
      ? await client.GET('/v1/users/me')
      : await client.GET('/v1/users/{id}', { params });
  if (apiError) {
    if (response.status === 401) redirect(303, '/login');
    error(response.status, apiError.message);
  }

  return { profile: data };
};
