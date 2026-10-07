import { renderComponent, type ColumnDef } from '@tanstack/svelte-table';

import type { DataTableFeatures } from '#lib/components/data-table-features.js';
import { Checkbox } from '#lib/components/ui/checkbox/index.js';
import { sortableColumn } from '#lib/components/users/sortable-column.js';
import type { AdminUser } from '#lib/types/user.js';

import DataTableActions from './data-table-actions.svelte';

export const columns: ColumnDef<DataTableFeatures, AdminUser>[] = [
  {
    id: 'select',
    header: ({ table }) =>
      renderComponent(Checkbox, {
        checked: table.getIsAllPageRowsSelected(),
        indeterminate: table.getIsSomePageRowsSelected() && !table.getIsAllPageRowsSelected(),
        onCheckedChange: (value: boolean) => table.toggleAllPageRowsSelected(value),
        'aria-label': 'Select all',
      }),
    cell: ({ row }) =>
      renderComponent(Checkbox, {
        checked: row.getIsSelected(),
        onCheckedChange: (value: boolean) => row.toggleSelected(value),
        'aria-label': 'Select row',
      }),
    enableSorting: false,
    enableHiding: false,
  },
  sortableColumn('username', 'Username'),
  sortableColumn('email', 'Email'),
  sortableColumn('website', 'Website'),
  sortableColumn('country', 'Country'),
  sortableColumn('role', 'Role'),
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
    cell: ({ row }) =>
      renderComponent(DataTableActions, { id: row.original.id, banned: row.original.isBanned ?? false }),
  },
];
