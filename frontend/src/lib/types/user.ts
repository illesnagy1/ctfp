import type { AdminUserResponse, AdminUserUpdateRequest, UserResponse } from '#lib/api/index.js';

export function isAdminUser(user: UserResponse): user is AdminUserResponse {
  return 'email' in user;
}

/** Copies only the fields the api accepts; it rejects unknown keys. */
export function toAdminUserUpdate(user: AdminUserResponse): AdminUserUpdateRequest {
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
