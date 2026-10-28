import { z } from 'zod';

import { m } from '#lib/paraglide/messages.js';

import { count } from './fields.js';

export const decayFunctions = ['static', 'linear', 'logarithmic', 'parabolic'] as const;

/** The add-flag dialog; posted as JSON (`dataType: 'json'`). Mirrors the api's checks in FlagService.kt. */
export const createFlagSchema = z
  .object({
    // A regular expression; the api checks it compiles (Java's syntax, which differs from JavaScript's in places).
    pattern: z
      .string()
      .min(1, { error: () => m.validation_required() })
      .max(255, { error: () => m.validation_too_long({ max: 255 }) }),
    isCaseSensitive: z.boolean().default(true),
    baseValue: count().default(100),
    decayFun: z.enum(decayFunctions).default('static'),
    // Only used when the value decays; sent as null for static flags.
    decayValue: count().default(10),
    minValue: count().default(10),
  })
  .refine((flag) => flag.decayFun === 'static' || flag.minValue <= flag.baseValue, {
    path: ['minValue'],
    error: () => m.validation_flag_min_value(),
  });

/** A player's flag guess. */
export const submitFlagSchema = z.object({
  flag: z
    .string()
    .trim()
    .min(1, { error: () => m.validation_required() })
    .max(1024, { error: () => m.validation_too_long({ max: 1024 }) }),
});

/** What the submit action reports back as the form's message. */
export type SubmitFlagResult = { correct: boolean; alreadySolved: boolean; attemptsLeft: number | null };
