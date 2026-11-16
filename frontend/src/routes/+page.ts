import { redirect } from '@sveltejs/kit';

// shortcut: no landing page yet, so the home page is the challenge list; replace with event info once settings have it.
export function load() {
  redirect(307, '/challenges');
}
