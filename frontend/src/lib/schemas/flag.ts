import { z } from 'zod';

import { m } from '#lib/paraglide/messages.js';

import { zFlagRequest, zSubmitFlagRequest } from './api.js';
import { trimmed } from './fields.js';

const decayFun = zFlagRequest.shape.decayFun.unwrap();
export const decayFunctions = decayFun.options;

/** A limit the dialog fills in, where the api takes null for none. */
const count = (schema: typeof zFlagRequest.shape.decayValue) => schema.unwrap().unwrap();

/** The add-flag dialog; posted as JSON (`dataType: 'json'`). */
export const createFlagSchema = zFlagRequest
  .omit({ sortOrder: true })
  .extend({
    // A regular expression; the api checks it compiles (Java's syntax, which differs from JavaScript's in places).
    pattern: zFlagRequest.shape.pattern,
    isCaseSensitive: z.boolean().default(true),
    baseValue: zFlagRequest.shape.baseValue.default(100),
    decayFun: decayFun.default('static'),
    // Only used when the value decays; sent as null for static flags.
    decayValue: count(zFlagRequest.shape.decayValue).default(10),
    minValue: count(zFlagRequest.shape.minValue).default(10),
  })
  // What the annotations can't say; FlagService.kt checks it too.
  .refine((flag) => flag.decayFun === 'static' || flag.minValue <= flag.baseValue, {
    path: ['minValue'],
    error: () => m.validation_flag_min_value(),
  });

/** A player's flag guess. */
export const submitFlagSchema = z.object({ flag: trimmed(zSubmitFlagRequest.shape.flag) });

/** What the submit action reports back as the form's message. */
export type SubmitFlagResult = { correct: boolean; alreadySolved: boolean; attemptsLeft: number | null };
