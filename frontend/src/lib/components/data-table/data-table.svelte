<script lang="ts" generics="TData extends RowData">
  import { createTable, FlexRender, type ColumnDef, type RowData } from '@tanstack/svelte-table';

  import * as Table from '#lib/components/ui/table/index.js';

  import DataTablePagination from './data-table-pagination.svelte';
  import DataTableToolbar from './data-table-toolbar.svelte';
  import { features, type DataTableFeatures } from './features.js';
  import type { DataTableFilter, DataTableSearch } from './types.js';

  let {
    data,
    columns,
    search,
    filters,
  }: {
    data: TData[];
    columns: ColumnDef<DataTableFeatures, TData>[];
    /** Text search shown in the toolbar. */
    search?: DataTableSearch;
    /** Faceted filters shown in the toolbar. */
    filters?: DataTableFilter[];
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
    enableRowSelection: true,
  });
</script>

<div class="space-y-4">
  <DataTableToolbar {table} {search} {filters} />
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
          <Table.Row data-state={row.getIsSelected() && 'selected'}>
            {#each row.getVisibleCells() as cell (cell.id)}
              <Table.Cell>
                <FlexRender {cell} />
              </Table.Cell>
            {/each}
          </Table.Row>
        {:else}
          <Table.Row>
            <Table.Cell colspan={columns.length} class="h-24 text-center">No results.</Table.Cell>
          </Table.Row>
        {/each}
      </Table.Body>
    </Table.Root>
  </div>
  <DataTablePagination {table} />
</div>
