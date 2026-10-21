import { applyAction, deserialize, type ActionResult } from '$app/forms';
import { refreshAll } from '$app/navigation';

import { m } from '#lib/paraglide/messages.js';

/**
 * Why a form action didn't succeed: the `message` of a `fail()` or a thrown `error()`, with a generic fallback.
 * `undefined` for success and redirects.
 */
export function actionError(result: ActionResult): string | undefined {
  if (result.type === 'failure') {
    const message = result.data?.message;
    return typeof message === 'string' ? message : m.form_action_failed();
  }
  if (result.type === 'error') return (result.error as App.Error | undefined)?.message ?? m.form_action_failed();
  return undefined;
}

/**
 * Calls a SvelteKit form action from code, the way `use:enhance` does for a form: reloads the page's data on success,
 * follows redirects, and throws with the action's message on a failure, e.g. for `confirmDelete`'s `onConfirm`.
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

  // Thrown `error()`s are reported too, rather than replacing the whole page with the error page.
  const error = actionError(result);
  if (error) throw new Error(error);

  if (result.type === 'success') await refreshAll();
  else await applyAction(result);
}
