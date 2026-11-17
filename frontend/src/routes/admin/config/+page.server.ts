import type { InitialFormData } from '@sjsf/sveltekit3';
import { createAction } from '@sjsf/sveltekit3/server';
import { adapt } from '@sjsf/zod4-validator/classic';
import { error, type Actions } from '@sveltejs/kit';

import { getSettings, updateSettings } from '#lib/api/index.js';
import type { Settings } from '#lib/api/index.js';
import { status } from '#lib/http.js';
import { settingsSchema } from '#lib/schemas/settings.js';
import { apiClient, call } from '#lib/server/api.js';
import * as defaults from '#lib/sjsf/defaults.js';
import { settingsJsonSchema } from '#lib/types/schemas.js';

import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async (event) => {
  const { data, error: apiError, response } = await call(getSettings({ client: apiClient(event) }));
  if (apiError) error(response.status, apiError.message);

  return { settingsForm: { initialValue: data } satisfies InitialFormData<Settings> };
};

export const actions = {
  default: createAction(
    { ...defaults, ...adapt(settingsSchema), name: 'settingsForm', sendData: true },
    async (settings, event) => {
      // Form actions don't run the /admin layout guard; the api checks the role again either way.
      if (event.locals.user?.role !== 'ADMIN') error(status.FORBIDDEN, 'Admin access required');

      const { error: apiError } = await call(updateSettings({ client: apiClient(event), body: settings }));
      if (!apiError) return undefined;
      // The api's messages start with the setting's name (e.g. "maxTeamSize must be…"); put them on that field.
      const field = Object.keys(settingsJsonSchema.properties).find((key) => apiError.message.startsWith(key));
      return [{ path: field ? [field] : [], message: apiError.message }];
    },
  ),
} satisfies Actions;
