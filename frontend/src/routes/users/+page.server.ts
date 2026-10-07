import { ApiError, api } from '$lib/server/api';
import { type AdminUser, type User, toAdminUserUpdate } from '$lib/types/user';
import { error, fail, redirect } from '@sveltejs/kit';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  if (!event.locals.user) redirect(303, '/login');

  // Admins get AdminUser rows (incl. hidden and banned users); everyone else gets the public User view.
  const users = await api<User[]>(event, '/v1/users').catch((e) => {
    if (e instanceof ApiError && e.status === 401) redirect(303, '/login');
    throw e;
  });

  return { users };
};

export const actions: Actions = {
  setBanned: async (event) => {
    if (event.locals.user?.role !== 'ADMIN') error(403, 'Admin access required');

    const form = await event.request.formData();
    const id = Number(form.get('id'));
    const banned = form.get('banned') === 'true';
    if (!Number.isInteger(id)) return fail(400, { message: 'Invalid user id' });

    try {
      // The api has no PATCH yet, so send the full admin update with only isBanned changed.
      const user = await api<AdminUser>(event, `/v1/users/${id}`);
      await api(event, `/v1/users/${id}`, {
        method: 'PUT',
        body: JSON.stringify({ ...toAdminUserUpdate(user), isBanned: banned }),
      });
    } catch (e) {
      if (e instanceof ApiError && e.status < 500) return fail(e.status, { message: e.message });
      throw e;
    }
  },
};
