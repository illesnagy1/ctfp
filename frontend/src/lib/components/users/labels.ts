import type { DataTableOption } from '#lib/components/data-table/index.js';
import { m } from '#lib/paraglide/messages.js';
import type { AdminUser } from '#lib/types/user.js';

/** Role options in the current locale; call it where it renders, not at module level. */
export const roleOptions = (): DataTableOption[] => [
  { value: 'ADMIN', label: m.users_role_admin() },
  { value: 'USER', label: m.users_role_user() },
];

export const roleLabel = (role: AdminUser['role']) => roleOptions().find((o) => o.value === role)?.label ?? role;

export type UserStatus = 'banned' | 'hidden' | 'unverified';

/** Status options in the current locale; call it where it renders, not at module level. */
export const statusOptions = (): DataTableOption[] => [
  { value: 'banned', label: m.users_status_banned() },
  { value: 'hidden', label: m.users_status_hidden() },
  { value: 'unverified', label: m.users_status_unverified() },
];

/** The user's banned, hidden and unverified flags, as stable keys for filtering. */
export const userStatuses = (user: AdminUser): UserStatus[] => [
  ...(user.isBanned ? (['banned'] as const) : []),
  ...(user.isHidden ? (['hidden'] as const) : []),
  ...(user.isVerified ? [] : (['unverified'] as const)),
];

export const statusLabel = (status: UserStatus) => statusOptions().find((o) => o.value === status)?.label ?? status;
