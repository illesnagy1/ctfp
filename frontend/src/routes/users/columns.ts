import { renderComponent, type ColumnDef } from '@tanstack/svelte-table';

import { sortableColumn, type DataTableFeatures } from '#lib/components/data-table/index.js';
import { m } from '#lib/paraglide/messages.js';
import type { User } from '#lib/types/user.js';

import RowActions from './row-actions.svelte';

export const columns: ColumnDef<DataTableFeatures, User>[] = [
  sortableColumn('username', m.users_column_username),
  sortableColumn('website', m.users_column_website),
  sortableColumn('country', m.users_column_country),
  {
    id: 'actions',
    enableSorting: false,
    enableHiding: false,
    cell: ({ row }) => renderComponent(RowActions, { id: row.original.id }),
  },
];
