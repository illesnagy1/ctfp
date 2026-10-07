import { fail, redirect } from '@sveltejs/kit';

import { ApiError, api } from '#lib/server/api.js';
import { type Session, createSession } from '#lib/server/session.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = ({ locals }) => {
  if (locals.user) redirect(303, '/');
};

export const actions: Actions = {
  default: async (event) => {
    const form = await event.request.formData();
    const username = form.get('username')?.toString() ?? '';
    const email = form.get('email')?.toString() ?? '';
    const password = form.get('password')?.toString() ?? '';
    const confirmPassword = form.get('confirmPassword')?.toString() ?? '';
    const registrationCode = form.get('registrationCode')?.toString() || undefined;
    const values = { username, email, registrationCode };

    if (password !== confirmPassword) return fail(400, { ...values, message: 'Passwords do not match' });

    try {
      const session = await api<Session>(event, '/v1/auth/register', {
        method: 'POST',
        body: JSON.stringify({ username, email, password, registrationCode }),
      });
      await createSession(event.cookies, session);
    } catch (error) {
      if (error instanceof ApiError && error.status < 500)
        return fail(error.status, { ...values, message: error.message });
      console.error('Registration failed', error);
      return fail(500, { ...values, message: 'Something went wrong, try again later' });
    }

    redirect(303, '/');
  },
};
