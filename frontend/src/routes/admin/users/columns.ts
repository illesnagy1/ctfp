import { renderComponent, type ColumnDef } from '@tanstack/svelte-table';

import {
  DataTableColumnHeader,
  includesValue,
  selectColumn,
  sortableColumn,
  type DataTableFeatures,
} from '#lib/components/data-table/index.js';
import { roleLabel, statusLabel } from '#lib/components/users/labels.js';
import { m } from '#lib/paraglide/messages.js';
import type { AdminUser } from '#lib/types/user.js';

import RowActions from './row-actions.svelte';

export const columns: ColumnDef<DataTableFeatures, AdminUser>[] = [
  selectColumn(),
  sortableColumn('username', m.users_column_username),
  sortableColumn('email', m.users_column_email),
  sortableColumn('website', m.users_column_website),
  sortableColumn('country', m.users_column_country),
  {
    ...sortableColumn('role', m.users_column_role),
    cell: ({ row }) => roleLabel(row.original.role),
    filterFn: includesValue,
  },
  {
    id: 'status',
    meta: { title: m.users_column_status },
    header: ({ column }) =>
      renderComponent(DataTableColumnHeader<AdminUser, unknown>, { title: m.users_column_status(), column }),
    accessorFn: statusLabel,
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
