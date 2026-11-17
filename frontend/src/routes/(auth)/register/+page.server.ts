import { fail, redirect } from '@sveltejs/kit';
import { superValidate } from 'sveltekit-superforms';
import { zod4 } from 'sveltekit-superforms/adapters';

import { register } from '#lib/api/index.js';
import { status } from '#lib/http.js';
import { registerSchema } from '#lib/schemas/auth.js';
import { apiClient, call } from '#lib/server/api.js';
import { apiFormError } from '#lib/server/form.js';
import { createSession } from '#lib/server/session.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ locals }) => {
  if (locals.user) redirect(status.SEE_OTHER, '/');
  return { form: await superValidate(zod4(registerSchema)) };
};

export const actions: Actions = {
  default: async (event) => {
    const form = await superValidate(event, zod4(registerSchema));
    const { username, email, password, registrationCode } = form.data;
    // The form is sent back on failure; never echo the passwords.
    form.data.password = '';
    form.data.confirmPassword = '';
    if (!form.valid) return fail(status.BAD_REQUEST, { form });

    const { data, error, response } = await call(
      register({
        client: apiClient(event),
        body: { username, email, password, registrationCode: registrationCode || undefined },
      }),
    );
    if (error) return apiFormError(form, response, error, 'Registration');

    await createSession(event.cookies, data);
    return redirect(status.SEE_OTHER, '/');
  },
};
