import { fail, redirect } from '@sveltejs/kit';
import { superValidate } from 'sveltekit-superforms';
import { zod4 } from 'sveltekit-superforms/adapters';

import { forgotPassword } from '#lib/api/index.js';
import { status } from '#lib/http.js';
import { forgotPasswordSchema } from '#lib/schemas/auth.js';
import { apiClient, call } from '#lib/server/api.js';
import { apiFormError } from '#lib/server/form.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ locals, url }) => {
  if (locals.user) redirect(status.SEE_OTHER, '/');
  return { form: await superValidate(zod4(forgotPasswordSchema)), sent: url.searchParams.has('sent') };
};

export const actions: Actions = {
  default: async (event) => {
    const form = await superValidate(event, zod4(forgotPasswordSchema));
    if (!form.valid) return fail(status.BAD_REQUEST, { form });

    // The api answers the same for unknown addresses, so this never says whether an account exists.
    const { response } = await call(forgotPassword({ client: apiClient(event), body: form.data }));
    if (!response.ok) return apiFormError(form, response, undefined, 'Password reset request');

    return redirect(status.SEE_OTHER, '?sent');
  },
};
