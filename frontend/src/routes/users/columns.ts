import { resolve } from '$app/paths';
import { renderComponent, type ColumnDef } from '@tanstack/svelte-table';

import {
  DataTableBadgesCell,
  DataTableColumnHeader,
  DataTableLinkCell,
  includesValue,
  selectColumn,
  sortableColumn,
  type DataTableFeatures,
} from '#lib/components/data-table/index.js';
import { roleLabel, statusLabel, userStatuses } from '#lib/components/users/labels.js';
import { m } from '#lib/paraglide/messages.js';
import type { AdminUser, User } from '#lib/types/user.js';

import RowActions from './row-actions.svelte';

/** The username, linking to the user's page. */
const usernameColumn = <T extends User>(): ColumnDef<DataTableFeatures, T> => ({
  ...sortableColumn<T>('username', m.users_column_username),
  cell: ({ row }) =>
    renderComponent(DataTableLinkCell, {
      href: resolve('/users/[id]', { id: String(row.original.id) }),
      text: row.original.username,
    }),
});

/** The public list, as players see it. */
export const columns: ColumnDef<DataTableFeatures, User>[] = [
  usernameColumn(),
  sortableColumn('website', m.users_column_website),
  sortableColumn('country', m.users_column_country),
];

/** The full list for admins: email, role and statuses, row selection for bulk actions, and the ban menu. */
export const adminColumns: ColumnDef<DataTableFeatures, AdminUser>[] = [
  selectColumn(),
  usernameColumn(),
  sortableColumn('email', m.users_column_email),
  sortableColumn('website', m.users_column_website),
  sortableColumn('country', m.users_column_country),
  {
    ...sortableColumn('role', m.users_column_role),
    cell: ({ row }) => renderComponent(DataTableBadgesCell, { badges: [roleLabel(row.original.role)] }),
    filterFn: includesValue,
  },
  {
    id: 'status',
    meta: { title: m.users_column_status },
    header: ({ column }) =>
      renderComponent(DataTableColumnHeader<AdminUser, unknown>, { title: m.users_column_status(), column }),
    accessorFn: userStatuses,
    enableSorting: false,
    cell: ({ row }) => renderComponent(DataTableBadgesCell, { badges: userStatuses(row.original).map(statusLabel) }),
    // A user can have several statuses: facet on each, and match if any is selected (like challenge categories).
    getUniqueValues: userStatuses,
    filterFn: (row, _id, selected: string[]) => userStatuses(row.original).some((s) => selected.includes(s)),
  },
  {
    id: 'actions',
    enableSorting: false,
    enableHiding: false,
    cell: ({ row }) =>
      renderComponent(RowActions, {
        id: row.original.id,
        username: row.original.username,
        banned: row.original.isBanned ?? false,
      }),
  },
];
