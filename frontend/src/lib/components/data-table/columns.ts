import { renderComponent, type ColumnDef, type RowData } from '@tanstack/svelte-table';

import { Checkbox } from '#lib/components/ui/checkbox/index.js';
import { m } from '#lib/paraglide/messages.js';

import DataTableColumnHeader from './data-table-column-header.svelte';
import type { DataTableFeatures } from './features.js';
import { resolveText, type Text } from './types.js';

/** Column for `key` with a sortable header. Spread it to add a cell, filter, etc. */
export function sortableColumn<TData extends RowData>(
  key: keyof TData & string,
  title: Text,
): ColumnDef<DataTableFeatures, TData> {
  return {
    accessorKey: key,
    meta: { title },
    // renderComponent can't infer the header's generics, so instantiate them explicitly.
    header: ({ column }) =>
      renderComponent(DataTableColumnHeader<TData, unknown>, { title: resolveText(title), column }),
  };
}

/** Checkbox column for row selection, with select-all for the current page in the header. */
export function selectColumn<TData extends RowData>(): ColumnDef<DataTableFeatures, TData> {
  return {
    id: 'select',
    header: ({ table }) =>
      renderComponent(Checkbox, {
        checked: table.getIsAllPageRowsSelected(),
        indeterminate: table.getIsSomePageRowsSelected() && !table.getIsAllPageRowsSelected(),
        onCheckedChange: (value: boolean) => table.toggleAllPageRowsSelected(value),
        'aria-label': m.data_table_select_all(),
      }),
    cell: ({ row }) =>
      renderComponent(Checkbox, {
        checked: row.getIsSelected(),
        onCheckedChange: (value: boolean) => row.toggleSelected(value),
        'aria-label': m.data_table_select_row(),
      }),
    enableSorting: false,
    enableHiding: false,
  };
}

/** `filterFn` for a faceted filter column: matches rows whose value is one of the selected values. */
export function includesValue(row: { getValue: (id: string) => unknown }, id: string, value: unknown[]) {
  return value.includes(row.getValue(id));
}
