export type UserRole = 'ADMIN' | 'USER';

/** Public view of a user (api `UserResponse`). */
export type User = {
  id: number;
  username: string;
  country: string | null;
  website: string | null;
  /** ISO 8601 timestamp. */
  createdAt: string;
};

/** Full view of a user, returned to admins and to the user themselves (api `AdminUserResponse`). */
export type AdminUser = User & {
  codeId: number | null;
  email: string;
  language: string | null;
  role: UserRole;
  isVerified: boolean;
  isHidden: boolean;
  isBanned: boolean;
  totpEnabled: boolean;
  soundEnabled: boolean;
  visibilitySettings: string | null;
  updatedAt: string;
};

/** Body of `PUT /v1/users/{id}` when sent by an admin (api `AdminUserUpdateRequest`). */
export type AdminUserUpdate = Pick<
  AdminUser,
  | 'codeId'
  | 'username'
  | 'email'
  | 'language'
  | 'country'
  | 'website'
  | 'role'
  | 'isVerified'
  | 'isHidden'
  | 'isBanned'
  | 'soundEnabled'
  | 'visibilitySettings'
>;

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
