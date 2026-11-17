import { fail } from '@sveltejs/kit';
import { setError, type FormPathLeavesWithErrors, type SuperValidated } from 'sveltekit-superforms';
import type { z } from 'zod';

import { isClientError, isServerError, status } from '#lib/http.js';

import { problemFields, problemMessage } from './problem.js';

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

/** Whether [key] is one of the form's own fields, so a problem about it can be shown on it. */
function isFieldOf<T extends Record<string, unknown>>(
  form: SuperValidated<T>,
  key: string,
): key is FormPathLeavesWithErrors<T> {
  return Object.hasOwn(form.data, key);
}

/**
 * Reports a failed api call on a superform. For 4xx: the problem's field errors go on the form's fields of the same
 * name, anything else becomes the form-level message. For 5xx: a generic message, and a log entry.
 */
export function apiFormError<T extends Record<string, unknown>>(
  form: SuperValidated<T>,
  response: Response,
  apiError: unknown,
  what: string,
) {
  if (!isServerError(response.status)) {
    const fields = problemFields(apiError).filter(({ path }) => path.length === 1);
    for (const { path, detail } of fields) {
      const [key = ''] = path;
      if (isFieldOf(form, key)) setError(form, key, detail);
    }
    if (form.valid) form.message = problemMessage(apiError) ?? response.statusText;
    // What superforms' `message()` does, without its narrower status type.
    return fail(isClientError(response.status) ? response.status : status.BAD_REQUEST, { form });
  }
  console.error(`${what} failed`, response.status, apiError);
  form.message = 'Something went wrong, try again later';
  return fail(status.INTERNAL_SERVER_ERROR, { form });
}
