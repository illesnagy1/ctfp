import { error } from '@sveltejs/kit';

import { apiClient } from '#lib/server/api.js';

import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  // The route documents no error responses, so check the response itself.
  const { data, response } = await apiClient(event).GET('/v1/scoreboard');
  if (!data) error(response.status, response.statusText);

  return { entries: data };
};
