import { error, fail } from '@sveltejs/kit';

import { apiClient } from '#lib/server/api.js';
import { formString } from '#lib/server/form.js';
import type { components } from '#lib/types/api.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  // The route documents no error responses yet, so check the response itself.
  const { data, response } = await apiClient(event).GET('/v1/challenges');
  if (!data) error(response.status, response.statusText);

  return { challenges: data };
};

/** Parses a non-negative whole number field; empty means 0 (no limit). */
function formCount(form: FormData, name: string) {
  const value = Number(formString(form, name) || '0');
  return Number.isInteger(value) && value >= 0 ? value : undefined;
}

export const actions: Actions = {
  create: async (event) => {
    if (event.locals.user?.role !== 'ADMIN') error(403, 'Admin access required');

    const form = await event.request.formData();
    const title = formString(form, 'title').trim();
    const description = formString(form, 'description').trim() || null;
    // One field per tag.
    const categories = [
      ...new Set(
        form
          .getAll('categories')
          .filter((c) => typeof c === 'string')
          .map((c) => c.trim())
          .filter(Boolean),
      ),
    ];
    // Sent as ISO by the dialog, converted from the browser's local time.
    const publishAt = new Date(formString(form, 'publishAt'));
    const maxAttempts = formCount(form, 'maxAttempts');
    const timeLimit = formCount(form, 'timeLimit');
    const isVisible = form.has('isVisible');

    if (!title) return fail(400, { message: 'Title is required' });
    if (Number.isNaN(publishAt.getTime())) return fail(400, { message: 'Invalid publish date' });
    if (maxAttempts === undefined || timeLimit === undefined) {
      return fail(400, { message: 'Limits must be whole numbers, 0 or more' });
    }

    // The route documents no error responses yet, so read the api's ErrorResponse body untyped.
    const { error: apiError, response } = await apiClient(event).POST('/v1/challenges', {
      body: { title, description, categories, publishAt: publishAt.toISOString(), maxAttempts, timeLimit, isVisible },
    });
    if (!response.ok) {
      const message = (apiError as components['schemas']['ErrorResponse'] | undefined)?.message;
      if (response.status < 500) return fail(response.status, { message: message ?? response.statusText });
      console.error('Creating challenge failed', response.status, apiError);
      return fail(500, { message: 'Something went wrong, try again later' });
    }
    return { success: true };
  },
};
