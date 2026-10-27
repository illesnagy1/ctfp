import { error, fail } from '@sveltejs/kit';

import { idSchema } from '#lib/schemas/ids.js';
import { apiClient } from '#lib/server/api.js';
import { parseForm } from '#lib/server/form.js';
import { renderMarkdown } from '#lib/server/markdown.js';

import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  const id = Number(event.params.id);
  if (!Number.isInteger(id)) error(404, 'Challenge not found');

  const client = apiClient(event);
  const params = { path: { id: String(id) } };

  // The route documents no error responses yet, so check the response itself.
  const [challenge, files] = await Promise.all([
    client.GET('/v1/challenges/{id}', { params }),
    client.GET('/v1/challenges/{id}/files', { params }),
  ]);
  const { data, response } = challenge;
  if (!data) error(response.status, response.status === 404 ? 'Challenge not found' : response.statusText);
  // Same visibility as the challenge, so this only fails if something else went wrong.
  if (!files.data) error(files.response.status, files.error?.message ?? files.response.statusText);

  // Rendered here so the page never sees unsanitized markup.
  const descriptionHtml = data.description ? await renderMarkdown(data.description) : undefined;
  return { challenge: data, files: files.data, descriptionHtml };
};

export const actions: Actions = {
  deleteFile: async (event) => {
    if (event.locals.user?.role !== 'ADMIN') error(403, 'Admin access required');

    const { data, failure } = await parseForm(event.request, idSchema);
    if (failure) return failure;

    const { error: apiError, response } = await apiClient(event).DELETE('/v1/files/{id}', {
      params: { path: { id: String(data.id) } },
    });
    if (apiError) return fail(response.status, { message: apiError.message });
    return { success: true };
  },
};
