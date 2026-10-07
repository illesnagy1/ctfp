import { renderComponent, type ColumnDef, type RowData } from '@tanstack/svelte-table';

import DataTableColumnHeader from '#lib/components/data-table-column-header.svelte';
import type { DataTableFeatures } from '#lib/components/data-table-features.js';

export function sortableColumn<TData extends RowData>(
  key: keyof TData & string,
  title: string,
): ColumnDef<DataTableFeatures, TData> {
  return {
    accessorKey: key,
    // renderComponent can't infer the header's generics, so instantiate them explicitly.
    header: ({ column }) => renderComponent(DataTableColumnHeader<TData, unknown>, { title, column }),
  };
}
