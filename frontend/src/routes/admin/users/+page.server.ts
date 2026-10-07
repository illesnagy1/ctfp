import { error, fail, redirect } from '@sveltejs/kit';

import { apiClient } from '#lib/server/api.js';
import { toAdminUserUpdate } from '#lib/types/user.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  const { data, error: apiError, response } = await apiClient(event).GET('/v1/admin/users');
  if (apiError) {
    if (response.status === 401) redirect(303, '/login');
    error(response.status, apiError.message);
  }

  return { users: data };
};

export const actions: Actions = {
  setBanned: async (event) => {
    if (event.locals.user?.role !== 'ADMIN') error(403, 'Admin access required');

    const form = await event.request.formData();
    const id = Number(form.get('id'));
    const banned = form.get('banned') === 'true';
    if (!Number.isInteger(id)) return fail(400, { message: 'Invalid user id' });

    const client = apiClient(event);
    const params = { path: { id: String(id) } };

    const fetched = await client.GET('/v1/admin/users/{id}', { params });
    if (fetched.error) return fail(fetched.response.status, { message: fetched.error.message });

    // The api has no PATCH yet, so send the full admin update with only isBanned changed.
    const updated = await client.PUT('/v1/admin/users/{id}', {
      params,
      body: { ...toAdminUserUpdate(fetched.data), isBanned: banned },
    });
    if (updated.error) return fail(updated.response.status, { message: updated.error.message });
  },
};
