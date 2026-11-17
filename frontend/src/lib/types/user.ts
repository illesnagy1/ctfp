import type { AdminUserResponse, UserResponse } from '#lib/api/index.js';

export function isAdminUser(user: UserResponse): user is AdminUserResponse {
  return 'email' in user;
}
