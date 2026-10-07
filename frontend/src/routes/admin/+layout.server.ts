import { error, redirect } from '@sveltejs/kit';

import type { LayoutServerLoad } from './$types';

/**
 * Guards every page under /admin. Form actions don't run layout loads, so admin actions must check the role
 * themselves; the api checks it again either way.
 */
export const load: LayoutServerLoad = ({ locals }) => {
  if (!locals.user) redirect(303, '/login');
  if (locals.user.role !== 'ADMIN') error(403, 'Admin access required');
};
