import { redirect } from '@sveltejs/kit';

import { status } from '#lib/http.js';

// shortcut: no landing page yet, so the home page is the challenge list; replace with event info once settings have it.
export function load() {
  redirect(status.TEMPORARY_REDIRECT, '/challenges');
}
