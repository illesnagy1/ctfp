import { ApiError, api } from '$lib/server/api';
import type { User } from '$lib/types/user';
import { error, redirect } from '@sveltejs/kit';

import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  if (!event.locals.user) redirect(303, '/login');

  const id = Number(event.params.id);
  if (!Number.isInteger(id)) error(404, 'User not found');

  // AdminUser for admins and the user themselves, public User otherwise.
  const user = await api<User>(event, `/v1/users/${id}`).catch((e) => {
    if (e instanceof ApiError && e.status === 401) redirect(303, '/login');
    if (e instanceof ApiError && e.status === 404) error(404, 'User not found');
    throw e;
  });

  return { profile: user };
};
