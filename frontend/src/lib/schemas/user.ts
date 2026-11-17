import { z } from 'zod';

import type { AdminUserResponse } from '#lib/api/index.js';

import { zAdminUserCreateRequest, zAdminUserUpdateRequest } from './api.js';
import { country, email, username, website } from './fields.js';

/** The admin add-user dialog; posted as JSON (`dataType: 'json'`). */
export const createUserSchema = zAdminUserCreateRequest.pick({ password: true }).extend({
  username,
  email,
  role: zAdminUserCreateRequest.shape.role.default('USER'),
  isVerified: z.boolean().default(true),
  isHidden: z.boolean().default(false),
});

/** Ban or unban, as posted by the row menu's form and `submitAction`. */
export const setBannedSchema = z.object({
  banned: z.stringbool(),
});

/**
 * The edit-profile dialog; posted as JSON. Users edit their profile fields, admins the account fields too (the action
 * ignores those for anyone else). Empty country and website mean none.
 */
export const editUserSchema = z.object({
  username,
  email,
  country,
  website,
  role: zAdminUserUpdateRequest.shape.role.default('USER'),
  isVerified: z.boolean().default(false),
  isHidden: z.boolean().default(false),
  isBanned: z.boolean().default(false),
});

/** The edit form's starting values from the saved profile; empty strings for none. */
export const editUserData = (user: AdminUserResponse): z.infer<typeof editUserSchema> => ({
  username: user.username,
  email: user.email,
  country: user.country ?? '',
  website: user.website ?? '',
  role: user.role,
  isVerified: user.isVerified ?? false,
  isHidden: user.isHidden ?? false,
  isBanned: user.isBanned ?? false,
});

/** Superform id of a user's edit form; unique, so the users table can hold several. */
export const editUserFormId = (id: number) => `edit-user-${id}`;
