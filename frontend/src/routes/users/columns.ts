import type { ColumnDef } from '@tanstack/table-core';

import { renderComponent } from '#lib/components/ui/data-table/index.js';
import { sortableColumn } from '#lib/components/users/sortable-column.js';
import type { User } from '#lib/types/user.js';

import DataTableActions from './data-table-actions.svelte';

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
