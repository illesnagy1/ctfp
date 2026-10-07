import type { DataTableOption } from '#lib/components/data-table/index.js';
import { m } from '#lib/paraglide/messages.js';
import type { AdminUser } from '#lib/types/user.js';

/** Role options in the current locale; call it where it renders, not at module level. */
export const roleOptions = (): DataTableOption[] => [
  { value: 'ADMIN', label: m.users_role_admin() },
  { value: 'USER', label: m.users_role_user() },
];

export const roleLabel = (role: AdminUser['role']) => roleOptions().find((o) => o.value === role)?.label ?? role;

/** Banned, hidden and unverified flags as one comma-separated label. */
export const statusLabel = (user: AdminUser) =>
  [
    user.isBanned && m.users_status_banned(),
    user.isHidden && m.users_status_hidden(),
    !user.isVerified && m.users_status_unverified(),
  ]
    .filter(Boolean)
    .join(', ');
