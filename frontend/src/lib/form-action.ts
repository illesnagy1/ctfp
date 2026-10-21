import { applyAction, deserialize } from '$app/forms';
import { invalidateAll } from '$app/navigation';

import { m } from '#lib/paraglide/messages.js';

/**
 * Calls a SvelteKit form action from code, the way `use:enhance` does for a form: reloads the page's data on success,
 * follows redirects, and throws with the action's `message` on a failure, e.g. for `confirmDelete`'s `onConfirm`.
 *
 * ```ts
 * await submitAction('?/delete', { id: '1' });
 * ```
 */
export async function submitAction(action: string, fields: Record<string, string>) {
  const body = new FormData();
  for (const [name, value] of Object.entries(fields)) body.set(name, value);

  const response = await fetch(action, { method: 'POST', body, headers: { 'x-sveltekit-action': 'true' } });
  const result = deserialize(await response.text());

  if (result.type === 'failure') {
    const message = result.data?.message;
    throw new Error(typeof message === 'string' ? message : m.form_action_failed());
  }
  // Thrown `error()`s would otherwise replace the whole page with the error page.
  if (result.type === 'error')
    throw new Error((result.error as App.Error | undefined)?.message ?? m.form_action_failed());

  if (result.type === 'success') await invalidateAll();
  else await applyAction(result);
}
