import type { components } from '#lib/types/api.js';

type Schemas = components['schemas'];

export type User = Schemas['UserResponse'];

export type AdminUserUpdate = Schemas['AdminUserUpdateRequest'];

/** Full view of a user, returned by `/v1/admin/users` and `/v1/users/me`. */
export type AdminUser = Schemas['AdminUserResponse'];

export function isAdminUser(user: User): user is AdminUser {
  return 'email' in user;
}

/** Copies only the fields the api accepts; it rejects unknown keys. */
export function toAdminUserUpdate(user: AdminUser): AdminUserUpdate {
  return {
    codeId: user.codeId,
    username: user.username,
    email: user.email,
    language: user.language,
    country: user.country,
    website: user.website,
    role: user.role,
    isVerified: user.isVerified,
    isHidden: user.isHidden,
    isBanned: user.isBanned,
    soundEnabled: user.soundEnabled,
    visibilitySettings: user.visibilitySettings,
  };
}
