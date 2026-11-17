import { fail, redirect } from '@sveltejs/kit';
import { superValidate } from 'sveltekit-superforms';
import { zod4 } from 'sveltekit-superforms/adapters';

import { resetPassword } from '#lib/api/index.js';
import { status } from '#lib/http.js';
import { resetPasswordSchema } from '#lib/schemas/auth.js';
import { apiClient, call } from '#lib/server/api.js';
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
    if (!form.valid) return fail(status.BAD_REQUEST, { form });

    const { error, response } = await call(resetPassword({ client: apiClient(event), body: { token, password } }));
    if (error) return apiFormError(form, response, error, 'Password reset');

    return redirect(status.SEE_OTHER, '/login?reset');
  },
};
