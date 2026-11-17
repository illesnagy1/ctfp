import { error, fail, redirect, type RequestEvent } from '@sveltejs/kit';
import { superValidate } from 'sveltekit-superforms';
import { zod4 } from 'sveltekit-superforms/adapters';

import { createUser, listAdminUsers, listUsers, patchUser, patchUsers } from '#lib/api/index.js';
import type { ProblemDetails } from '#lib/api/index.js';
import { status } from '#lib/http.js';
import { idSchema, idsSchema } from '#lib/schemas/ids.js';
import { createUserSchema, setBannedSchema } from '#lib/schemas/user.js';
import { apiClient, call } from '#lib/server/api.js';
import { apiFormError, parseForm } from '#lib/server/form.js';
import { problemMessage } from '#lib/server/problem.js';

import type { Actions, PageServerLoad } from './$types';

/**
 * Admins get the full list (email, role, banned and hidden users too) and the add-user form; everyone else the public
 * list, which leaves out banned and hidden users.
 */
export const load: PageServerLoad = async (event) => {
  const { user } = event.locals;
  if (!user) redirect(status.SEE_OTHER, '/login');
  const client = apiClient(event);

  if (user.role === 'ADMIN') {
    const { data, error: apiError, response } = await call(listAdminUsers({ client }));
    if (apiError) fail401OrError(response, apiError);
    return { admin: true as const, users: data, createForm: await superValidate(zod4(createUserSchema)) };
  }

  const { data, error: apiError, response } = await call(listUsers({ client }));
  if (apiError) fail401OrError(response, apiError);
  return { admin: false as const, users: data };
};

function fail401OrError(response: Response, apiError: ProblemDetails): never {
  if (response.status === status.UNAUTHORIZED) redirect(status.SEE_OTHER, '/login');
  error(response.status, problemMessage(apiError));
}

/** The page's load decides the view, but actions don't run loads, so each action checks the role itself. */
function requireAdmin(event: RequestEvent) {
  const user = event.locals.user;
  if (user?.role !== 'ADMIN') error(status.FORBIDDEN, 'Admin access required');
  return user;
}

export const actions: Actions = {
  create: async (event) => {
    requireAdmin(event);

    const form = await superValidate(event, zod4(createUserSchema));
    const body = form.data;
    // The form is sent back to the browser; never echo the password.
    form.data = { ...form.data, password: '' };
    if (!form.valid) return fail(status.BAD_REQUEST, { form });

    const { error: apiError, response } = await call(createUser({ client: apiClient(event), body }));
    if (apiError) return apiFormError(form, response, apiError, 'Creating user');
    return { form };
  },

  setBanned: async (event) => {
    requireAdmin(event);

    // The api refuses an admin's own ban, with a message shown as the toast.
    const { data, failure } = await parseForm(event.request, idSchema.extend(setBannedSchema.shape));
    if (failure) return failure;
    const { id, banned } = data;

    // Only isBanned changes (JSON Merge Patch).
    const { error: apiError, response } = await call(
      patchUser({ client: apiClient(event), path: { id: String(id) }, body: { isBanned: banned } }),
    );
    if (apiError) return fail(response.status, { message: problemMessage(apiError) });
    return { success: true };
  },

  setBannedMany: async (event) => {
    requireAdmin(event);

    const { data, failure } = await parseForm(event.request, idsSchema.extend(setBannedSchema.shape));
    if (failure) return failure;
    const { ids, banned } = data;

    // One bulk patch, so it's all or nothing; a user that no longer exists makes it a 404.
    const body = Object.fromEntries(ids.map((id) => [id, { isBanned: banned }]));
    const { error: apiError, response } = await call(patchUsers({ client: apiClient(event), body }));
    if (apiError) return fail(response.status, { message: problemMessage(apiError) });
    return { success: true };
  },
};
