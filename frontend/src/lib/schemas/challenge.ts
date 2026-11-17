import { z } from 'zod';

import { zChallengeRequest } from './api.js';
import { trimmed } from './fields.js';

/** The add-challenge dialog; posted as JSON (`dataType: 'json'`) for the categories array and typed numbers. */
export const createChallengeSchema = zChallengeRequest.extend({
  title: trimmed(zChallengeRequest.shape.title),
  // Empty for none.
  description: z.string().trim(),
  categories: z.array(z.string().trim().min(1)).overwrite((categories) => [...new Set(categories)]),
  // An absolute time: the dialog converts from the browser's local time, since the server can't know its zone.
  publishAt: zChallengeRequest.shape.publishAt,
  isVisible: z.boolean().default(true),
});
