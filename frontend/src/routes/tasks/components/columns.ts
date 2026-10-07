import { createColumnHelper, renderComponent, renderSnippet } from '@tanstack/svelte-table';
import { createRawSnippet } from 'svelte';

import {
  DataTableBadgeCell,
  DataTableColumnHeader,
  DataTableOptionCell,
  includesValue,
  selectColumn,
  type DataTableFeatures,
} from '#lib/components/data-table/index.js';

import { labels, priorities, statuses } from '../data/data.js';
import type { Task } from '../data/schemas';
import RowActions from './row-actions.svelte';

const columnHelper = createColumnHelper<DataTableFeatures, Task>();

export const columns = columnHelper.columns([
  selectColumn<Task>(),
  columnHelper.accessor('id', {
    header: ({ column }) => renderComponent(DataTableColumnHeader<Task, string>, { column, title: 'Task' }),
    cell: ({ row }) => {
      const idSnippet = createRawSnippet<[{ id: string }]>((getId) => {
        const { id } = getId();
        return {
          render: () => `<div class="w-20">${id}</div>`,
        };
      });

      return renderSnippet(idSnippet, {
        id: row.original.id,
      });
    },
    enableSorting: false,
    enableHiding: false,
  }),
  columnHelper.accessor('title', {
    header: ({ column }) => renderComponent(DataTableColumnHeader<Task, string>, { column, title: 'Title' }),
    cell: ({ row }) =>
      renderComponent(DataTableBadgeCell, {
        value: row.original.title,
        badge: labels.find((l) => l.value === row.original.label)?.label,
      }),
  }),
  columnHelper.accessor('status', {
    header: ({ column }) => renderComponent(DataTableColumnHeader<Task, string>, { column, title: 'Status' }),
    cell: ({ row }) =>
      renderComponent(DataTableOptionCell, { value: row.original.status, options: statuses, class: 'w-25' }),
    filterFn: includesValue,
  }),
  columnHelper.accessor('priority', {
    header: ({ column }) => renderComponent(DataTableColumnHeader<Task, string>, { column, title: 'Priority' }),
    cell: ({ row }) => renderComponent(DataTableOptionCell, { value: row.original.priority, options: priorities }),
    filterFn: includesValue,
  }),
  columnHelper.display({
    id: 'actions',
    cell: ({ row }) => renderComponent(RowActions, { row }),
  }),
]);
