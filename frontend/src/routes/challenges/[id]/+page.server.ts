import { error, fail } from '@sveltejs/kit';
import { message, setError, superValidate } from 'sveltekit-superforms';
import { zod4 } from 'sveltekit-superforms/adapters';

import {
  deleteFile,
  deleteFlag,
  getChallenge,
  listChallengeFiles,
  listChallengeFlags,
  createFlag,
  submitFlag,
} from '#lib/api/index.js';
import { status } from '#lib/http.js';
import { m } from '#lib/paraglide/messages.js';
import { createFlagSchema, submitFlagSchema, type SubmitFlagResult } from '#lib/schemas/flag.js';
import { idSchema } from '#lib/schemas/ids.js';
import { apiClient, call } from '#lib/server/api.js';
import { apiFormError, parseForm } from '#lib/server/form.js';
import { renderMarkdown } from '#lib/server/markdown.js';
import { problemMessage } from '#lib/server/problem.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  const id = Number(event.params.id);
  if (!Number.isInteger(id)) error(status.NOT_FOUND, 'Challenge not found');

  const client = apiClient(event);
  const params = { path: { id: String(id) } };
  const isAdmin = event.locals.user?.role === 'ADMIN';

  const [challenge, files, flags] = await Promise.all([
    call(getChallenge({ client, ...params })),
    call(listChallengeFiles({ client, ...params })),
    // The answers: admins only, and the api refuses everyone else anyway.
    isAdmin ? call(listChallengeFlags({ client, ...params })) : undefined,
  ]);
  const { data, error: apiError, response } = challenge;
  if (apiError)
    error(response.status, response.status === status.NOT_FOUND ? 'Challenge not found' : problemMessage(apiError));
  // Same visibility as the challenge, so this only fails if something else went wrong.
  if (files.error) error(files.response.status, problemMessage(files.error));
  if (flags?.error) error(flags.response.status, problemMessage(flags.error));

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
    if (!event.locals.user) error(status.UNAUTHORIZED, 'Log in to submit flags');

    const form = await superValidate(event, zod4(submitFlagSchema));
    if (!form.valid) return fail(status.BAD_REQUEST, { form });

    const {
      data,
      error: apiError,
      response,
    } = await call(
      submitFlag({
        client: apiClient(event),
        path: { id: event.params.id },
        body: { flag: form.data.flag },
      }),
    );
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
    if (event.locals.user?.role !== 'ADMIN') error(status.FORBIDDEN, 'Admin access required');

    const form = await superValidate(event, zod4(createFlagSchema));
    if (!form.valid) return fail(status.BAD_REQUEST, { form });

    const { decayFun, decayValue, minValue, ...flag } = form.data;
    const decays = decayFun !== 'static';
    const { error: apiError, response } = await call(
      createFlag({
        client: apiClient(event),
        path: { id: event.params.id },
        body: { ...flag, decayFun, decayValue: decays ? decayValue : null, minValue: decays ? minValue : null },
      }),
    );
    // Patterns are unique across all challenges.
    if (response.status === status.CONFLICT) return setError(form, 'pattern', m.validation_flag_taken());
    // E.g. not a valid regular expression (Java's syntax): show it on the field; other errors become the form message.
    const detail = problemMessage(apiError);
    if (response.status === status.BAD_REQUEST && detail?.startsWith('Pattern'))
      return setError(form, 'pattern', detail);
    if (!response.ok) return apiFormError(form, response, apiError, 'Creating flag');
    return { form };
  },

  deleteFlag: async (event) => {
    if (event.locals.user?.role !== 'ADMIN') error(status.FORBIDDEN, 'Admin access required');

    const { data, failure } = await parseForm(event.request, idSchema);
    if (failure) return failure;

    const { error: apiError, response } = await call(
      deleteFlag({ client: apiClient(event), path: { id: String(data.id) } }),
    );
    if (apiError) return fail(response.status, { message: problemMessage(apiError) });
    return { success: true };
  },

  deleteFile: async (event) => {
    if (event.locals.user?.role !== 'ADMIN') error(status.FORBIDDEN, 'Admin access required');

    const { data, failure } = await parseForm(event.request, idSchema);
    if (failure) return failure;

    const { error: apiError, response } = await call(
      deleteFile({ client: apiClient(event), path: { id: String(data.id) } }),
    );
    if (apiError) return fail(response.status, { message: problemMessage(apiError) });
    return { success: true };
  },
};
