import { error } from '@sveltejs/kit';

import { apiClient } from '#lib/server/api.js';

import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  // The route documents no error responses yet, so check the response itself.
  const { data, response } = await apiClient(event).GET('/v1/challenges');
  if (!data) error(response.status, response.statusText);

  return { challenges: data };
};
