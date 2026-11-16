import { fail, redirect } from '@sveltejs/kit';
import { superValidate } from 'sveltekit-superforms';
import { zod4 } from 'sveltekit-superforms/adapters';

import { forgotPasswordSchema } from '#lib/schemas/auth.js';
import { apiClient } from '#lib/server/api.js';
import { apiFormError } from '#lib/server/form.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ locals, url }) => {
  if (locals.user) redirect(303, '/');
  return { form: await superValidate(zod4(forgotPasswordSchema)), sent: url.searchParams.has('sent') };
};

export const actions: Actions = {
  default: async (event) => {
    const form = await superValidate(event, zod4(forgotPasswordSchema));
    if (!form.valid) return fail(400, { form });

    // The api answers the same for unknown addresses, so this never says whether an account exists.
    const { response } = await apiClient(event).POST('/v1/auth/forgot-password', { body: form.data });
    if (!response.ok) return apiFormError(form, response, undefined, 'Password reset request');

    return redirect(303, '?sent');
  },
};
