import { renderComponent, type ColumnDef } from '@tanstack/svelte-table';

import { sortableColumn, type DataTableFeatures } from '#lib/components/data-table/index.js';
import type { User } from '#lib/types/user.js';

import RowActions from './row-actions.svelte';

export const columns: ColumnDef<DataTableFeatures, User>[] = [
  sortableColumn('username', 'Username'),
  sortableColumn('website', 'Website'),
  sortableColumn('country', 'Country'),
  {
    id: 'actions',
    enableSorting: false,
    enableHiding: false,
    cell: ({ row }) => renderComponent(RowActions, { id: row.original.id }),
  },
];
