import { fail, redirect } from '@sveltejs/kit';

import { apiClient } from '#lib/server/api.js';
import { formString } from '#lib/server/form.js';
import { createSession } from '#lib/server/session.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = ({ locals }) => {
  if (locals.user) redirect(303, '/');
};

export const actions: Actions = {
  default: async (event) => {
    const form = await event.request.formData();
    const username = formString(form, 'username');
    const email = formString(form, 'email');
    const password = formString(form, 'password');
    const confirmPassword = formString(form, 'confirmPassword');
    const registrationCode = formString(form, 'registrationCode') || undefined;
    const values = { username, email, registrationCode };

    if (password !== confirmPassword) return fail(400, { ...values, message: 'Passwords do not match' });

    const { data, error, response } = await apiClient(event).POST('/v1/auth/register', {
      body: { username, email, password, registrationCode },
    });
    if (error) {
      if (response.status < 500) return fail(response.status, { ...values, message: error.message });
      console.error('Registration failed', response.status, error);
      return fail(500, { ...values, message: 'Something went wrong, try again later' });
    }

    await createSession(event.cookies, data);
    return redirect(303, '/');
  },
};
