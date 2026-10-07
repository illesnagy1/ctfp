import type { Column, ColumnDef } from '@tanstack/table-core';

import { Checkbox } from '#lib/components/ui/checkbox/index.js';
import { DataTableColumnHeader, renderComponent } from '#lib/components/ui/data-table/index.js';
import type { AdminUser, User } from '#lib/types/user.js';

import DataTableActions from './data-table-actions.svelte';

function sortableColumn<T>(key: keyof T & string, title: string): ColumnDef<T> {
  return {
    accessorKey: key,
    // renderComponent can't infer the header's generics, so it expects Column<unknown>.
    header: ({ column }) => renderComponent(DataTableColumnHeader, { title, column: column as Column<unknown> }),
  };
}

export const columns: ColumnDef<User>[] = [
  sortableColumn('username', 'Username'),
  sortableColumn('website', 'Website'),
  sortableColumn('country', 'Country'),
  {
    id: 'actions',
    enableSorting: false,
    enableHiding: false,
    cell: ({ row }) => renderComponent(DataTableActions, { id: row.original.id }),
  },
];

export const adminColumns: ColumnDef<AdminUser>[] = [
  {
    id: 'select',
    header: ({ table }) =>
      renderComponent(Checkbox, {
        checked: table.getIsAllPageRowsSelected(),
        indeterminate: table.getIsSomePageRowsSelected() && !table.getIsAllPageRowsSelected(),
        onCheckedChange: (value) => table.toggleAllPageRowsSelected(!!value),
        'aria-label': 'Select all',
      }),
    cell: ({ row }) =>
      renderComponent(Checkbox, {
        checked: row.getIsSelected(),
        onCheckedChange: (value) => row.toggleSelected(!!value),
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
    cell: ({ row }) => renderComponent(DataTableActions, { id: row.original.id, banned: row.original.isBanned }),
  },
];
