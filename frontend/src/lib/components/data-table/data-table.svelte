<script lang="ts" generics="TData extends RowData">
  import { createTable, FlexRender, type ColumnDef, type RowData } from '@tanstack/svelte-table';
  import type { Snippet } from 'svelte';

  import * as Table from '#lib/components/ui/table/index.js';
  import { m } from '#lib/paraglide/messages.js';

  import DataTablePagination from './data-table-pagination.svelte';
  import DataTableToolbar from './data-table-toolbar.svelte';
  import { features, type DataTableFeatures } from './features.js';
  import type { DataTableFilter, DataTableSearch } from './types.js';

  let {
    data,
    columns,
    search,
    filters,
    selectable = true,
    getRowId,
    actions,
    isHighlighted,
  }: {
    data: TData[];
    columns: ColumnDef<DataTableFeatures, TData>[];
    /** Text search shown in the toolbar. */
    search?: DataTableSearch;
    /** Faceted filters shown in the toolbar. */
    filters?: DataTableFilter[];
    /** Whether rows can be selected; pair it with a `selectColumn()`. */
    selectable?: boolean;
    /**
     * Stable id of a row, e.g. its database id. Without it rows are keyed by index, so after rows are added or removed
     * the selection points at different rows.
     */
    getRowId?: (row: TData) => string;
    /** Toolbar buttons for the selected rows, e.g. a bulk delete. */
    actions?: Snippet<[selected: TData[]]>;
    /** Rows to show with a success background, e.g. solved challenges. */
    isHighlighted?: (row: TData) => boolean;
  } = $props();

  // v9 keeps pagination, sorting, filters, visibility and selection internally, and its reads are rune-reactive.
  const table = createTable({
    features,
    get data() {
      return data;
    },
    get columns() {
      return columns;
    },
    get enableRowSelection() {
      return selectable;
    },
    // Falls back to tanstack's default, the row's index path.
    getRowId: (row, index, parent) => getRowId?.(row) ?? (parent ? `${parent.id}.${index}` : String(index)),
  });

  const selected = $derived(table.getFilteredSelectedRowModel().rows.map((row) => row.original));
</script>

<div class="space-y-4">
  <DataTableToolbar {table} {search} {filters}>
    {#if actions}{@render actions(selected)}{/if}
  </DataTableToolbar>
  <div class="rounded-md border">
    <Table.Root>
      <Table.Header>
        {#each table.getHeaderGroups() as headerGroup (headerGroup.id)}
          <Table.Row>
            {#each headerGroup.headers as header (header.id)}
              <Table.Head colspan={header.colSpan}>
                {#if !header.isPlaceholder}
                  <FlexRender {header} />
                {/if}
              </Table.Head>
            {/each}
          </Table.Row>
        {/each}
      </Table.Header>
      <Table.Body>
        {#each table.getRowModel().rows as row (row.id)}
          <Table.Row
            data-state={row.getIsSelected() && 'selected'}
            data-highlighted={isHighlighted?.(row.original) || undefined}
          >
            {#each row.getVisibleCells() as cell (cell.id)}
              <Table.Cell>
                <FlexRender {cell} />
              </Table.Cell>
            {/each}
          </Table.Row>
        {:else}
          <Table.Row>
            <Table.Cell colspan={columns.length} class="h-24 text-center">{m.data_table_no_results()}</Table.Cell>
          </Table.Row>
        {/each}
      </Table.Body>
    </Table.Root>
  </div>
  <DataTablePagination {table} showSelection={selectable} />
</div>
