import { z } from 'zod';

import { email, newPassword, username } from './fields.js';

/** The admin add-user dialog; posted as JSON (`dataType: 'json'`). */
export const createUserSchema = z.object({
  username: username(),
  email: email(),
  password: newPassword(),
  role: z.enum(['ADMIN', 'USER']).default('USER'),
  isVerified: z.boolean().default(true),
  isHidden: z.boolean().default(false),
});

/** Ban or unban, as posted by the row menu's form and `submitAction`. */
export const setBannedSchema = z.object({
  banned: z.stringbool(),
});
