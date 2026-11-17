import { error, fail, redirect, type RequestEvent } from '@sveltejs/kit';
import { superValidate } from 'sveltekit-superforms';
import { zod4 } from 'sveltekit-superforms/adapters';

import {
  getAdminUser,
  listUserSubmissions,
  getUser,
  getMe,
  listMySubmissions,
  updateUser,
  updateMe,
} from '#lib/api/index.js';
import { status } from '#lib/http.js';
import { editUserData, editUserFormId, editUserSchema } from '#lib/schemas/user.js';
import { apiClient, call } from '#lib/server/api.js';
import { apiFormError } from '#lib/server/form.js';
import { problemMessage } from '#lib/server/problem.js';
import { isAdminUser, toAdminUserUpdate } from '#lib/types/user.js';

import type { Actions, PageServerLoad } from './$types';

function userId(event: RequestEvent) {
  const id = Number(event.params.id);
  if (!Number.isInteger(id)) error(status.NOT_FOUND, 'User not found');
  return id;
}

export const load: PageServerLoad = async (event) => {
  const { user } = event.locals;
  if (!user) redirect(status.SEE_OTHER, '/login');
  const id = userId(event);

  // Admins see everyone's full details (banned and hidden users too), others their own; anyone else's is the public
  // view, which hides banned and hidden users.
  const client = apiClient(event);
  const params = { path: { id: String(id) } };
  const {
    data,
    error: apiError,
    response,
  } = user.role === 'ADMIN'
    ? await call(getAdminUser({ client, ...params }))
    : id === user.id
      ? await call(getMe({ client }))
      : await call(getUser({ client, ...params }));
  if (apiError) {
    if (response.status === status.UNAUTHORIZED) redirect(status.SEE_OTHER, '/login');
    error(response.status, problemMessage(apiError));
  }

  // The full view comes exactly when the viewer may edit: their own profile, or any as an admin.
  const editForm = isAdminUser(data)
    ? await superValidate(editUserData(data), zod4(editUserSchema), { id: editUserFormId(id) })
    : undefined;

  // On your own profile, or any as an admin.
  const self = id === user.id;
  const submissions =
    user.role === 'ADMIN'
      ? await call(listUserSubmissions({ client, ...params }))
      : self
        ? await call(listMySubmissions({ client }))
        : undefined;
  if (submissions?.error) error(submissions.response.status, problemMessage(submissions.error));
  return { profile: data, editForm, admin: user.role === 'ADMIN', self, submissions: submissions?.data };
};

export const actions: Actions = {
  update: async (event) => {
    const { user } = event.locals;
    if (!user) redirect(status.SEE_OTHER, '/login');
    const id = userId(event);
    const admin = user.role === 'ADMIN';
    const self = id === user.id;
    if (!admin && !self) error(status.FORBIDDEN, 'You can only edit your own profile');

    const form = await superValidate(event, zod4(editUserSchema));
    if (!form.valid) return fail(status.BAD_REQUEST, { form });
    const { username, email, role, isVerified, isHidden, isBanned } = form.data;
    // Explicit nulls: the api requires the nullable fields to be present.
    const profile = {
      username,
      email,
      country: form.data.country || null,
      website: form.data.website || null,
    };

    // The api only takes full updates, so the fields the form doesn't show are sent back as they are.
    const client = apiClient(event);
    const params = { path: { id: String(id) } };
    const current = admin ? await call(getAdminUser({ client, ...params })) : await call(getMe({ client }));
    if (current.error) return apiFormError(form, current.response, current.error, 'Loading user');

    const { error: apiError, response } = admin
      ? await call(
          updateUser({
            client,
            ...params,
            body: {
              ...toAdminUserUpdate(current.data),
              ...profile,
              // The api refuses an admin's own demotion or ban; the dialog disables those fields for them.
              role,
              isBanned,
              isVerified,
              isHidden,
            },
          }),
        )
      : await call(
          updateMe({
            client,
            body: {
              ...profile,
              language: current.data.language,
              soundEnabled: current.data.soundEnabled,
              visibilitySettings: current.data.visibilitySettings,
            },
          }),
        );
    if (apiError) return apiFormError(form, response, apiError, 'Updating user');
    return { form };
  },
};
