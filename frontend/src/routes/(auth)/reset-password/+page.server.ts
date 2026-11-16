import { fail, redirect } from '@sveltejs/kit';
import { superValidate } from 'sveltekit-superforms';
import { zod4 } from 'sveltekit-superforms/adapters';

import { resetPasswordSchema } from '#lib/schemas/auth.js';
import { apiClient } from '#lib/server/api.js';
import { apiFormError } from '#lib/server/form.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ url }) => {
  const token = url.searchParams.get('token') ?? '';
  return { form: await superValidate({ token }, zod4(resetPasswordSchema), { errors: false }), hasToken: token !== '' };
};

export const actions: Actions = {
  default: async (event) => {
    const form = await superValidate(event, zod4(resetPasswordSchema));
    const { token, password } = form.data;
    // The form is sent back on failure; never echo the passwords.
    form.data.password = '';
    form.data.confirmPassword = '';
    if (!form.valid) return fail(400, { form });

    const { error, response } = await apiClient(event).POST('/v1/auth/reset-password', { body: { token, password } });
    if (error) return apiFormError(form, response, error, 'Password reset');

    return redirect(303, '/login?reset');
  },
};
