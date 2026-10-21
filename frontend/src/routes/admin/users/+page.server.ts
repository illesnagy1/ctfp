import { error, fail, redirect, type RequestEvent } from '@sveltejs/kit';

import { apiClient } from '#lib/server/api.js';
import { formString } from '#lib/server/form.js';
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

/** Form actions don't run the /admin layout guard, so each action checks the role itself. */
function requireAdmin(event: RequestEvent) {
  const user = event.locals.user;
  if (user?.role !== 'ADMIN') error(403, 'Admin access required');
  return user;
}

const SELF_BAN = 'You cannot ban yourself';

export const actions: Actions = {
  create: async (event) => {
    requireAdmin(event);

    const form = await event.request.formData();
    const role = formString(form, 'role');
    if (role !== 'ADMIN' && role !== 'USER') return fail(400, { message: 'Invalid role' });

    const { error: apiError, response } = await apiClient(event).POST('/v1/admin/users', {
      body: {
        username: formString(form, 'username').trim(),
        email: formString(form, 'email').trim(),
        password: formString(form, 'password'),
        role,
        isVerified: form.has('isVerified'),
        isHidden: form.has('isHidden'),
      },
    });
    if (apiError) return fail(response.status, { message: apiError.message });
    return { success: true };
  },

  setBanned: async (event) => {
    const self = requireAdmin(event);

    const form = await event.request.formData();
    const id = Number(form.get('id'));
    const banned = form.get('banned') === 'true';
    if (!Number.isInteger(id)) return fail(400, { message: 'Invalid user id' });
    // Banned users lose admin rights, so this would lock the admin out.
    if (banned && id === self.id) return fail(400, { message: SELF_BAN });

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
    return { success: true };
  },

  setBannedMany: async (event) => {
    const self = requireAdmin(event);

    const form = await event.request.formData();
    // Comma-separated, as sent by submitAction.
    const ids = new Set(formString(form, 'ids').split(',').map(Number));
    const banned = form.get('banned') === 'true';
    if (ids.size === 0 || ![...ids].every(Number.isInteger)) return fail(400, { message: 'Invalid user ids' });
    if (banned && ids.has(self.id)) return fail(400, { message: SELF_BAN });

    const client = apiClient(event);
    const fetched = await client.GET('/v1/admin/users');
    if (fetched.error) return fail(fetched.response.status, { message: fetched.error.message });

    // Full updates again (no PATCH), sent as one bulk PUT so it's all or nothing.
    const users = fetched.data.filter((user) => ids.has(user.id));
    if (users.length !== ids.size) return fail(404, { message: 'Some users no longer exist' });
    const body = Object.fromEntries(users.map((user) => [user.id, { ...toAdminUserUpdate(user), isBanned: banned }]));

    const updated = await client.PUT('/v1/admin/users', { body });
    if (updated.error) return fail(updated.response.status, { message: updated.error.message });
    return { success: true };
  },
};
