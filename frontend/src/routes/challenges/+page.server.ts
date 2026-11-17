import { error, fail } from '@sveltejs/kit';
import { setError, superValidate } from 'sveltekit-superforms';
import { zod4 } from 'sveltekit-superforms/adapters';

import { deleteChallenges, deleteChallenge, listChallenges, createChallenge } from '#lib/api/index.js';
import { isServerError, status } from '#lib/http.js';
import { m } from '#lib/paraglide/messages.js';
import { createChallengeSchema } from '#lib/schemas/challenge.js';
import { idSchema, idsSchema } from '#lib/schemas/ids.js';
import { apiClient, call } from '#lib/server/api.js';
import { apiFormError, parseForm } from '#lib/server/form.js';
import { problemMessage } from '#lib/server/problem.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  const client = apiClient(event);
  // Logged in, each challenge also says whether the caller solved it.
  const { data, error: apiError, response } = await call(listChallenges({ client }));
  if (apiError) error(response.status, problemMessage(apiError));

  return {
    challenges: data,
    createForm: await superValidate(zod4(createChallengeSchema)),
  };
};

/** For the id-only actions, which report failures as `{ message }` for `submitAction`. */
function apiFailure(response: Response, apiError: unknown, what: string) {
  if (!isServerError(response.status))
    return fail(response.status, { message: problemMessage(apiError) ?? response.statusText });
  console.error(`${what} failed`, response.status, apiError);
  return fail(status.INTERNAL_SERVER_ERROR, { message: 'Something went wrong, try again later' });
}

export const actions: Actions = {
  create: async (event) => {
    if (event.locals.user?.role !== 'ADMIN') error(status.FORBIDDEN, 'Admin access required');

    const form = await superValidate(event, zod4(createChallengeSchema));
    if (!form.valid) return fail(status.BAD_REQUEST, { form });

    const { description, ...challenge } = form.data;
    const { error: apiError, response } = await call(
      createChallenge({ client: apiClient(event), body: { ...challenge, description: description || null } }),
    );
    // Titles are unique, so a conflict can only be the title.
    if (response.status === status.CONFLICT) return setError(form, 'title', m.validation_title_taken());
    if (!response.ok) return apiFormError(form, response, apiError, 'Creating challenge');
    return { form };
  },

  delete: async (event) => {
    if (event.locals.user?.role !== 'ADMIN') error(status.FORBIDDEN, 'Admin access required');

    const { data, failure } = await parseForm(event.request, idSchema);
    if (failure) return failure;

    const { error: apiError, response } = await call(
      deleteChallenge({ client: apiClient(event), path: { id: String(data.id) } }),
    );
    if (!response.ok) return apiFailure(response, apiError, 'Deleting challenge');
    return { success: true };
  },

  deleteMany: async (event) => {
    if (event.locals.user?.role !== 'ADMIN') error(status.FORBIDDEN, 'Admin access required');

    const { data, failure } = await parseForm(event.request, idsSchema);
    if (failure) return failure;

    const { error: apiError, response } = await call(deleteChallenges({ client: apiClient(event), body: data.ids }));
    if (!response.ok) return apiFailure(response, apiError, 'Deleting challenges');
    return { success: true };
  },
};
