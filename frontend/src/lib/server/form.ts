import { fail } from '@sveltejs/kit';
import type { SuperValidated } from 'sveltekit-superforms';
import type { z } from 'zod';

/**
 * Parses an action's form fields with [schema]; for the id-only actions called by `submitAction`, which have no form
 * on the page. On failure returns the `fail()` to send back.
 */
export async function parseForm<T extends z.ZodType>(request: Request, schema: T) {
  const result = schema.safeParse(Object.fromEntries(await request.formData()));
  return result.success
    ? { data: result.data, failure: undefined }
    : { data: undefined, failure: fail(400, { message: result.error.issues[0]?.message ?? 'Invalid request' }) };
}

/**
 * Reports a failed api call on a superform as its form-level message: the api's own message for 4xx, a generic one
 * (and a log entry) for 5xx.
 */
export function apiFormError<T extends Record<string, unknown>>(
  form: SuperValidated<T>,
  response: Response,
  apiError: { message?: string } | undefined,
  what: string,
) {
  // What superforms' `message()` does, without its narrower status type.
  if (response.status < 500) {
    form.message = apiError?.message ?? response.statusText;
    return fail(response.status >= 400 ? response.status : 400, { form });
  }
  console.error(`${what} failed`, response.status, apiError);
  form.message = 'Something went wrong, try again later';
  return fail(500, { form });
}
