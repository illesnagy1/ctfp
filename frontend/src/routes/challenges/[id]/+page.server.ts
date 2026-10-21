import { error } from '@sveltejs/kit';

import { apiClient } from '#lib/server/api.js';
import { renderMarkdown } from '#lib/server/markdown.js';

import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  const id = Number(event.params.id);
  if (!Number.isInteger(id)) error(404, 'Challenge not found');

  // The route documents no error responses yet, so check the response itself.
  const { data, response } = await apiClient(event).GET('/v1/challenges/{id}', {
    params: { path: { id: String(id) } },
  });
  if (!data) error(response.status, response.status === 404 ? 'Challenge not found' : response.statusText);

  // Rendered here so the page never sees unsanitized markup.
  const descriptionHtml = data.description ? await renderMarkdown(data.description) : undefined;
  return { challenge: data, descriptionHtml };
};
