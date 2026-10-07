import { fail, redirect } from '@sveltejs/kit';

import { apiClient } from '#lib/server/api.js';
import { createSession } from '#lib/server/session.js';

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

    const { data, error, response } = await apiClient(event).POST('/v1/auth/login', { body: { login, password } });
    if (error) {
      if (response.status < 500) return fail(response.status, { login, message: error.message });
      console.error('Login failed', response.status, error);
      return fail(500, { login, message: 'Something went wrong, try again later' });
    }

    await createSession(event.cookies, data);
    redirect(303, '/');
  },
};
