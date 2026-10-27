import { fail, redirect } from '@sveltejs/kit';
import { superValidate } from 'sveltekit-superforms';
import { zod4 } from 'sveltekit-superforms/adapters';

import { loginSchema } from '#lib/schemas/auth.js';
import { apiClient } from '#lib/server/api.js';
import { apiFormError } from '#lib/server/form.js';
import { createSession } from '#lib/server/session.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ locals }) => {
  if (locals.user) redirect(303, '/');
  return { form: await superValidate(zod4(loginSchema)) };
};

export const actions: Actions = {
  default: async (event) => {
    const form = await superValidate(event, zod4(loginSchema));
    const { login, password } = form.data;
    // The form is sent back on failure; never echo the password.
    form.data.password = '';
    if (!form.valid) return fail(400, { form });

    const { data, error, response } = await apiClient(event).POST('/v1/auth/login', { body: { login, password } });
    if (error) return apiFormError(form, response, error, 'Login');

    await createSession(event.cookies, data);
    return redirect(303, '/');
  },
};
