import { createColumnHelper, renderComponent, renderSnippet } from '@tanstack/svelte-table';
import { createRawSnippet } from 'svelte';

import { Checkbox } from '#lib/components/ui/checkbox/index.js';

import type { Task } from '../data/schemas';
import DataTableColumnHeader from './data-table-column-header.svelte';
import type { TasksTableFeatures } from './data-table-features.js';
import DataTablePriorityCell from './data-table-priority-cell.svelte';
import DataTableRowActions from './data-table-row-actions.svelte';
import DataTableStatusCell from './data-table-status-cell.svelte';
import DataTableTitleCell from './data-table-title-cell.svelte';

const columnHelper = createColumnHelper<TasksTableFeatures, Task>();

export const columns = columnHelper.columns([
  columnHelper.display({
    id: 'select',
    header: ({ table }) =>
      renderComponent(Checkbox, {
        checked: table.getIsAllPageRowsSelected(),
        onCheckedChange: (value: boolean) => table.toggleAllPageRowsSelected(value),
        indeterminate: table.getIsSomePageRowsSelected() && !table.getIsAllPageRowsSelected(),
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
  }),
  columnHelper.accessor('id', {
    header: ({ column }) => {
      return renderComponent(DataTableColumnHeader<Task, string>, {
        column,
        title: 'Task',
      });
    },
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
    cell: ({ row }) => {
      return renderComponent(DataTableTitleCell, {
        labelValue: row.original.label,
        value: row.original.title,
      });
    },
  }),
  columnHelper.accessor('status', {
    header: ({ column }) =>
      renderComponent(DataTableColumnHeader<Task, string>, {
        column,
        title: 'Status',
      }),
    cell: ({ row }) => {
      return renderComponent(DataTableStatusCell, {
        value: row.original.status,
      });
    },
    filterFn: (row, id, value) => {
      return value.includes(row.getValue(id));
    },
  }),
  columnHelper.accessor('priority', {
    header: ({ column }) => {
      return renderComponent(DataTableColumnHeader<Task, string>, {
        title: 'Priority',
        column,
      });
    },
    cell: ({ row }) => {
      return renderComponent(DataTablePriorityCell, {
        value: row.original.priority,
      });
    },
    filterFn: (row, id, value) => {
      return value.includes(row.getValue(id));
    },
  }),
  columnHelper.display({
    id: 'actions',
    cell: ({ row }) => renderComponent(DataTableRowActions, { row }),
  }),
]);
