import { deleteSession } from '$lib/server/session';
import { redirect } from '@sveltejs/kit';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = ({ locals }) => {
  if (!locals.user) redirect(303, '/login');
};

// POST only, so a link or image on another site can't log people out.
export const actions: Actions = {
  default: async ({ cookies }) => {
    await deleteSession(cookies);
    redirect(303, '/login');
  },
};
