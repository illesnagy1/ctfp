import { z } from 'zod';

import { m } from '#lib/paraglide/messages.js';

/**
 * The api's request schemas, generated from openapi.json (`bun run gen:api`) with its limits, which the api enforces
 * from the same `@JsonSchema` annotations. Forms build on these rather than restating limits; import them from here,
 * so zod's messages below are translated.
 */
export * from '#lib/types/zod/zod.gen.js';

const messages: Record<string, unknown> = m;

/** `validation_<field>` for the issue's field, e.g. validation_username, which says what a valid one looks like. */
function fieldMessage(path: PropertyKey[] | undefined): string | undefined {
  const message = messages[`validation_${String(path?.at(-1))}`];
  return typeof message === 'function' ? String(message()) : undefined;
}

// Translates zod's own issues, since the generated schemas carry no messages. Runs when a field fails, in that
// request's locale; a schema's own `error` still wins. Unhandled issues keep zod's English default.
z.config({
  customError: (iss) => {
    switch (iss.code) {
      case 'too_small':
        if (iss.origin === 'string') {
          const min = Number(iss.minimum);
          return min <= 1 ? m.validation_required() : m.validation_too_short({ min });
        }
        if (iss.origin === 'number') return m.validation_min({ min: Number(iss.minimum) });
        return undefined;
      case 'too_big':
        if (iss.origin === 'string') return m.validation_too_long({ max: Number(iss.maximum) });
        if (iss.origin === 'number') return m.validation_max({ max: Number(iss.maximum) });
        return undefined;
      case 'invalid_format':
        if (iss.format === 'email') return m.validation_email();
        if (iss.format === 'datetime') return m.validation_date();
        return fieldMessage(iss.path) ?? m.validation_format();
      case 'invalid_union':
        return fieldMessage(iss.path);
      case 'invalid_type':
        return iss.expected === 'int' || iss.expected === 'number' ? m.validation_whole_number() : undefined;
      default:
        return undefined;
    }
  },
});
