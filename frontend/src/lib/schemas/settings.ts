import type { UiSchemaRoot } from '@sjsf/form';
import { z } from 'zod';

import { m } from '#lib/paraglide/messages.js';
import type { components } from '#lib/types/api.js';
import { settingsJsonSchema } from '#lib/types/schemas.js';

export type SettingsValue = components['schemas']['Settings'];

/**
 * The settings form's schema, built from the api's own (types, limits, descriptions; see dto/Settings.kt), so a new
 * setting there shows up on the config page after `bun run gen:api`. Typed with the api's Settings type, which
 * fromJSONSchema can't infer (the pipe adds the type and keeps the schema's limits).
 */
export const settingsSchema = z.fromJSONSchema(settingsJsonSchema).pipe(z.custom<SettingsValue>());

const messages: Record<string, unknown> = m;

/** The message [key] if there is one; looked up by name, so a missing one falls back instead of failing the build. */
function translate(key: string): string | undefined {
  const message = messages[key];
  return typeof message === 'function' ? String(message()) : undefined;
}

/** "maxTeamSize" → "Max team size": the label when there's no `settings_<key>` message yet. */
const humanize = (key: string) =>
  key
    .replace(/([A-Z])/g, ' $1')
    .replace(/^./, (c) => c.toUpperCase())
    .trim();

/**
 * Labels and help texts in the current locale: `settings_<key>` and `settings_<key>_description` messages, falling
 * back to the key and the schema's (English) description. Call it where it renders, not at module level.
 */
export function settingsUiSchema(): UiSchemaRoot {
  return Object.fromEntries(
    Object.entries(settingsJsonSchema.properties).map(([key, property]) => [
      key,
      {
        'ui:options': {
          title: translate(`settings_${key}`) ?? humanize(key),
          description: translate(`settings_${key}_description`) ?? property.description,
        },
      },
    ]),
  );
}
