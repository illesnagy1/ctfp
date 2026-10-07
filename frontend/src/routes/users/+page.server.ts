import { error, fail, redirect } from '@sveltejs/kit';

import { apiClient } from '#lib/server/api.js';
import { type AdminUser, type User, toAdminUserUpdate } from '#lib/types/user.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  if (!event.locals.user) redirect(303, '/login');

  const { data, error: apiError, response } = await apiClient(event).GET('/v1/users');
  if (apiError) {
    if (response.status === 401) redirect(303, '/login');
    error(response.status, apiError.message);
  }

  // Admins get AdminUser rows (incl. hidden and banned users); everyone else gets the public User view. The spec only
  // lists UserResponse until admin routes are split out (see the TODO in api/README.md).
  return { users: data as User[] };
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

    // Admins get an AdminUser here; see the note in `load`.
    const fetched = await client.GET('/v1/users/{id}', { params });
    if (fetched.error) return fail(fetched.response.status, { message: fetched.error.message });

    // The api has no PATCH yet, so send the full admin update with only isBanned changed. Typed as the user update
    // until admin routes are split out; the api accepts the admin fields from admins.
    const body = { ...toAdminUserUpdate(fetched.data as AdminUser), isBanned: banned };
    const updated = await client.PUT('/v1/users/{id}', { params, body });
    if (updated.error) return fail(updated.response.status, { message: updated.error.message });
  },
};
