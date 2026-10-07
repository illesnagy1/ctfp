import { renderComponent, type ColumnDef } from '@tanstack/svelte-table';

import {
  includesValue,
  selectColumn,
  sortableColumn,
  type DataTableFeatures,
  type DataTableOption,
} from '#lib/components/data-table/index.js';
import type { AdminUser } from '#lib/types/user.js';

import RowActions from './row-actions.svelte';

export const roles: DataTableOption[] = [
  { value: 'ADMIN', label: 'Admin' },
  { value: 'USER', label: 'User' },
];

export const columns: ColumnDef<DataTableFeatures, AdminUser>[] = [
  selectColumn(),
  sortableColumn('username', 'Username'),
  sortableColumn('email', 'Email'),
  sortableColumn('website', 'Website'),
  sortableColumn('country', 'Country'),
  { ...sortableColumn('role', 'Role'), filterFn: includesValue },
  {
    id: 'status',
    header: 'Status',
    accessorFn: (user) =>
      [user.isBanned && 'Banned', user.isHidden && 'Hidden', !user.isVerified && 'Unverified']
        .filter(Boolean)
        .join(', '),
  },
  {
    id: 'actions',
    enableSorting: false,
    enableHiding: false,
    cell: ({ row }) => renderComponent(RowActions, { id: row.original.id, banned: row.original.isBanned ?? false }),
  },
];
