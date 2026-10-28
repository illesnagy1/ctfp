import { error, fail } from '@sveltejs/kit';
import { message, setError, superValidate } from 'sveltekit-superforms';
import { zod4 } from 'sveltekit-superforms/adapters';

import { m } from '#lib/paraglide/messages.js';
import { createFlagSchema, submitFlagSchema, type SubmitFlagResult } from '#lib/schemas/flag.js';
import { idSchema } from '#lib/schemas/ids.js';
import { apiClient } from '#lib/server/api.js';
import { apiFormError, parseForm } from '#lib/server/form.js';
import { renderMarkdown } from '#lib/server/markdown.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  const id = Number(event.params.id);
  if (!Number.isInteger(id)) error(404, 'Challenge not found');

  const client = apiClient(event);
  const params = { path: { id: String(id) } };
  const isAdmin = event.locals.user?.role === 'ADMIN';

  // The route documents no error responses yet, so check the response itself.
  const [challenge, files, flags] = await Promise.all([
    client.GET('/v1/challenges/{id}', { params }),
    client.GET('/v1/challenges/{id}/files', { params }),
    // The answers: admins only, and the api refuses everyone else anyway.
    isAdmin ? client.GET('/v1/challenges/{id}/flags', { params }) : undefined,
  ]);
  const { data, response } = challenge;
  if (!data) error(response.status, response.status === 404 ? 'Challenge not found' : response.statusText);
  // Same visibility as the challenge, so this only fails if something else went wrong.
  if (!files.data) error(files.response.status, files.error?.message ?? files.response.statusText);
  if (flags && !flags.data) error(flags.response.status, flags.error?.message ?? flags.response.statusText);

  // Rendered here so the page never sees unsanitized markup.
  const descriptionHtml = data.description ? await renderMarkdown(data.description) : undefined;
  return {
    challenge: data,
    files: files.data,
    descriptionHtml,
    flags: flags?.data,
    // Two superforms on one page, so each needs its own id.
    createFlagForm: isAdmin ? await superValidate(zod4(createFlagSchema), { id: 'create-flag' }) : undefined,
    submitFlagForm: event.locals.user ? await superValidate(zod4(submitFlagSchema), { id: 'submit-flag' }) : undefined,
  };
};

export const actions: Actions = {
  submitFlag: async (event) => {
    if (!event.locals.user) error(401, 'Log in to submit flags');

    const form = await superValidate(event, zod4(submitFlagSchema));
    if (!form.valid) return fail(400, { form });

    const {
      data,
      error: apiError,
      response,
    } = await apiClient(event).POST('/v1/challenges/{id}/submissions', {
      params: { path: { id: event.params.id } },
      body: { flag: form.data.flag },
    });
    // E.g. 403 when banned or out of attempts.
    if (!data) return apiFormError(form, response, apiError, 'Submitting flag');
    const result: SubmitFlagResult = {
      correct: data.correct,
      alreadySolved: data.alreadySolved ?? false,
      attemptsLeft: data.attemptsLeft ?? null,
    };
    return message(form, result);
  },

  createFlag: async (event) => {
    if (event.locals.user?.role !== 'ADMIN') error(403, 'Admin access required');

    const form = await superValidate(event, zod4(createFlagSchema));
    if (!form.valid) return fail(400, { form });

    const { decayFun, decayValue, minValue, ...flag } = form.data;
    const decays = decayFun !== 'static';
    const { error: apiError, response } = await apiClient(event).POST('/v1/challenges/{id}/flags', {
      params: { path: { id: event.params.id } },
      body: { ...flag, decayFun, decayValue: decays ? decayValue : null, minValue: decays ? minValue : null },
    });
    // Patterns are unique across all challenges.
    if (response.status === 409) return setError(form, 'pattern', m.validation_flag_taken());
    // E.g. not a valid regular expression (Java's syntax): show it on the field; other errors become the form message.
    if (response.status === 400 && apiError?.message.startsWith('Pattern')) {
      return setError(form, 'pattern', apiError.message);
    }
    if (!response.ok) return apiFormError(form, response, apiError, 'Creating flag');
    return { form };
  },

  deleteFlag: async (event) => {
    if (event.locals.user?.role !== 'ADMIN') error(403, 'Admin access required');

    const { data, failure } = await parseForm(event.request, idSchema);
    if (failure) return failure;

    const { error: apiError, response } = await apiClient(event).DELETE('/v1/flags/{id}', {
      params: { path: { id: String(data.id) } },
    });
    if (apiError) return fail(response.status, { message: apiError.message });
    return { success: true };
  },

  deleteFile: async (event) => {
    if (event.locals.user?.role !== 'ADMIN') error(403, 'Admin access required');

    const { data, failure } = await parseForm(event.request, idSchema);
    if (failure) return failure;

    const { error: apiError, response } = await apiClient(event).DELETE('/v1/files/{id}', {
      params: { path: { id: String(data.id) } },
    });
    if (apiError) return fail(response.status, { message: apiError.message });
    return { success: true };
  },
};
