import { z } from 'zod';

import { m } from '#lib/paraglide/messages.js';

import { count } from './fields.js';

/** The add-challenge dialog; posted as JSON (`dataType: 'json'`) for the categories array and typed numbers. */
export const createChallengeSchema = z.object({
  title: z
    .string()
    .trim()
    .min(1, { error: () => m.validation_required() })
    .max(255, { error: () => m.validation_too_long({ max: 255 }) }),
  description: z.string().trim(),
  categories: z.array(z.string().trim().min(1)).transform((categories) => [...new Set(categories)]),
  // An absolute time: the dialog converts from the browser's local time, since the server can't know its zone.
  publishAt: z.iso.datetime({ error: () => m.validation_date() }),
  maxAttempts: count(),
  timeLimit: count(),
  isVisible: z.boolean().default(true),
});
