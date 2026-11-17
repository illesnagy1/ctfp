import { fail, redirect } from '@sveltejs/kit';
import { superValidate } from 'sveltekit-superforms';
import { zod4 } from 'sveltekit-superforms/adapters';

import { login as logIn } from '#lib/api/index.js';
import { status } from '#lib/http.js';
import { loginSchema } from '#lib/schemas/auth.js';
import { apiClient, call } from '#lib/server/api.js';
import { apiFormError } from '#lib/server/form.js';
import { createSession } from '#lib/server/session.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ locals, url }) => {
  if (locals.user) redirect(status.SEE_OTHER, '/');
  // Set by the reset-password page after a new password was saved.
  return { form: await superValidate(zod4(loginSchema)), passwordReset: url.searchParams.has('reset') };
};

export const actions: Actions = {
  default: async (event) => {
    const form = await superValidate(event, zod4(loginSchema));
    const { login, password } = form.data;
    // The form is sent back on failure; never echo the password.
    form.data.password = '';
    if (!form.valid) return fail(status.BAD_REQUEST, { form });

    const { data, error, response } = await call(logIn({ client: apiClient(event), body: { login, password } }));
    if (error) return apiFormError(form, response, error, 'Login');

    await createSession(event.cookies, data);
    return redirect(status.SEE_OTHER, '/');
  },
};
