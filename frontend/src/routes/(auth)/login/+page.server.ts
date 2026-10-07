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
    const login = form.get('login')?.toString() ?? '';
    const password = form.get('password')?.toString() ?? '';

    if (!login || !password) return fail(400, { login, message: 'Enter your username or email and password' });

    try {
      const session = await api<Session>(event, '/v1/auth/login', {
        method: 'POST',
        body: JSON.stringify({ login, password }),
      });
      await createSession(event.cookies, session);
    } catch (error) {
      if (error instanceof ApiError && error.status < 500) return fail(error.status, { login, message: error.message });
      console.error('Login failed', error);
      return fail(500, { login, message: 'Something went wrong, try again later' });
    }

    redirect(303, '/');
  },
};
