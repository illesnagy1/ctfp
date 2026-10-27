import { error, fail } from '@sveltejs/kit';
import { setError, superValidate } from 'sveltekit-superforms';
import { zod4 } from 'sveltekit-superforms/adapters';

import { m } from '#lib/paraglide/messages.js';
import { createChallengeSchema } from '#lib/schemas/challenge.js';
import { idSchema, idsSchema } from '#lib/schemas/ids.js';
import { apiClient } from '#lib/server/api.js';
import { apiFormError, parseForm } from '#lib/server/form.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  // The route documents no error responses yet, so check the response itself.
  const { data, response } = await apiClient(event).GET('/v1/challenges');
  if (!data) error(response.status, response.statusText);

  return { challenges: data, createForm: await superValidate(zod4(createChallengeSchema)) };
};

/** For the id-only actions, which report failures as `{ message }` for `submitAction`. */
function apiFailure(response: Response, apiError: { message?: string } | undefined, what: string) {
  if (response.status < 500) return fail(response.status, { message: apiError?.message ?? response.statusText });
  console.error(`${what} failed`, response.status, apiError);
  return fail(500, { message: 'Something went wrong, try again later' });
}

export const actions: Actions = {
  create: async (event) => {
    if (event.locals.user?.role !== 'ADMIN') error(403, 'Admin access required');

    const form = await superValidate(event, zod4(createChallengeSchema));
    if (!form.valid) return fail(400, { form });

    const { description, ...challenge } = form.data;
    const { error: apiError, response } = await apiClient(event).POST('/v1/challenges', {
      body: { ...challenge, description: description || null },
    });
    // Titles are unique, so a conflict can only be the title.
    if (response.status === 409) return setError(form, 'title', m.validation_title_taken());
    if (!response.ok) return apiFormError(form, response, apiError, 'Creating challenge');
    return { form };
  },

  delete: async (event) => {
    if (event.locals.user?.role !== 'ADMIN') error(403, 'Admin access required');

    const { data, failure } = await parseForm(event.request, idSchema);
    if (failure) return failure;

    const { error: apiError, response } = await apiClient(event).DELETE('/v1/challenges/{id}', {
      params: { path: { id: String(data.id) } },
    });
    if (!response.ok) return apiFailure(response, apiError, 'Deleting challenge');
    return { success: true };
  },

  deleteMany: async (event) => {
    if (event.locals.user?.role !== 'ADMIN') error(403, 'Admin access required');

    const { data, failure } = await parseForm(event.request, idsSchema);
    if (failure) return failure;

    const { error: apiError, response } = await apiClient(event).DELETE('/v1/challenges', { body: data.ids });
    if (!response.ok) return apiFailure(response, apiError, 'Deleting challenges');
    return { success: true };
  },
};
