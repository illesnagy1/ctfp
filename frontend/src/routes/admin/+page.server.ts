import { redirect } from '@sveltejs/kit';

import { status } from '#lib/http.js';

export function load() {
  redirect(status.PERMANENT_REDIRECT, '/admin/config');
}
