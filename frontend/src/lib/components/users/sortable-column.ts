import type { Column, ColumnDef } from '@tanstack/table-core';

import { DataTableColumnHeader, renderComponent } from '#lib/components/ui/data-table/index.js';

export function sortableColumn<T>(key: keyof T & string, title: string): ColumnDef<T> {
  return {
    accessorKey: key,
    // renderComponent can't infer the header's generics, so it expects Column<unknown>.
    header: ({ column }) => renderComponent(DataTableColumnHeader, { title, column: column as Column<unknown> }),
  };
}
