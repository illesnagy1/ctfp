import { fail } from '@sveltejs/kit';
import type { SuperValidated } from 'sveltekit-superforms';
import type { z } from 'zod';

import { isClientError, isServerError, status } from '#lib/http.js';

/**
 * Parses an action's form fields with [schema]; for the id-only actions called by `submitAction`, which have no form
 * on the page. On failure returns the `fail()` to send back.
 */
export async function parseForm<T extends z.ZodType>(request: Request, schema: T) {
  const result = schema.safeParse(Object.fromEntries(await request.formData()));
  return result.success
    ? { data: result.data, failure: undefined }
    : {
        data: undefined,
        failure: fail(status.BAD_REQUEST, { message: result.error.issues[0]?.message ?? 'Invalid request' }),
      };
}

/** The api's `{ message }` from a failed call's error, which is untyped for routes that document no errors. */
export function apiMessage(apiError: unknown): string | undefined {
  return typeof apiError === 'object' &&
    apiError !== null &&
    'message' in apiError &&
    typeof apiError.message === 'string'
    ? apiError.message
    : undefined;
}

/**
 * Reports a failed api call on a superform as its form-level message: the api's own message for 4xx, a generic one
 * (and a log entry) for 5xx.
 */
export function apiFormError<T extends Record<string, unknown>>(
  form: SuperValidated<T>,
  response: Response,
  apiError: unknown,
  what: string,
) {
  // What superforms' `message()` does, without its narrower status type.
  if (!isServerError(response.status)) {
    form.message = apiMessage(apiError) ?? response.statusText;
    return fail(isClientError(response.status) ? response.status : status.BAD_REQUEST, { form });
  }
  console.error(`${what} failed`, response.status, apiError);
  form.message = 'Something went wrong, try again later';
  return fail(status.INTERNAL_SERVER_ERROR, { form });
}
