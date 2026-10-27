import { fail, redirect } from '@sveltejs/kit';
import { superValidate } from 'sveltekit-superforms';
import { zod4 } from 'sveltekit-superforms/adapters';

import { registerSchema } from '#lib/schemas/auth.js';
import { apiClient } from '#lib/server/api.js';
import { apiFormError } from '#lib/server/form.js';
import { createSession } from '#lib/server/session.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ locals }) => {
  if (locals.user) redirect(303, '/');
  return { form: await superValidate(zod4(registerSchema)) };
};

export const actions: Actions = {
  default: async (event) => {
    const form = await superValidate(event, zod4(registerSchema));
    const { username, email, password, registrationCode } = form.data;
    // The form is sent back on failure; never echo the passwords.
    form.data.password = '';
    form.data.confirmPassword = '';
    if (!form.valid) return fail(400, { form });

    const { data, error, response } = await apiClient(event).POST('/v1/auth/register', {
      body: { username, email, password, registrationCode: registrationCode || undefined },
    });
    if (error) return apiFormError(form, response, error, 'Registration');

    await createSession(event.cookies, data);
    return redirect(303, '/');
  },
};
