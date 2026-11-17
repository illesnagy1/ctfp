import { error, redirect } from '@sveltejs/kit';

import { status } from '#lib/http.js';

import type { LayoutServerLoad } from './$types';

/**
 * Guards every page under /admin. Form actions don't run layout loads, so admin actions must check the role
 * themselves; the api checks it again either way.
 */
export const load: LayoutServerLoad = ({ locals }) => {
  if (!locals.user) redirect(status.SEE_OTHER, '/login');
  if (locals.user.role !== 'ADMIN') error(status.FORBIDDEN, 'Admin access required');
};
