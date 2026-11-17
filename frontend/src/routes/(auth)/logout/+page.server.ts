import { redirect } from '@sveltejs/kit';

import { status } from '#lib/http.js';
import { deleteSession } from '#lib/server/session.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = ({ locals }) => {
  if (!locals.user) redirect(status.SEE_OTHER, '/login');
};

// POST only, so a link or image on another site can't log people out.
export const actions: Actions = {
  default: async ({ cookies }) => {
    await deleteSession(cookies);
    redirect(status.SEE_OTHER, '/login');
  },
};
