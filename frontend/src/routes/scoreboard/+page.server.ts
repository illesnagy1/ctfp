import { error } from '@sveltejs/kit';

import { getScoreboard } from '#lib/api/index.js';
import { apiClient, call } from '#lib/server/api.js';
import { problemMessage } from '#lib/server/problem.js';

import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  const { data, error: apiError, response } = await call(getScoreboard({ client: apiClient(event) }));
  if (apiError) error(response.status, problemMessage(apiError));

  return { entries: data };
};
