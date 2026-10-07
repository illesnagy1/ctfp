<script lang="ts">
  import { createTable, FlexRender } from '@tanstack/svelte-table';

  import * as Table from '#lib/components/ui/table/index.js';

  import type { Task } from '../data/schemas.js';
  import { columns } from './columns.js';
  import { features } from './data-table-features.js';
  import DataTablePagination from './data-table-pagination.svelte';
  import DataTableToolbar from './data-table-toolbar.svelte';

  let { data }: { data: Task[] } = $props();

  // v9 keeps pagination, sorting, filters, visibility and selection internally, and its reads are rune-reactive.
  const table = createTable({
    features,
    get data() {
      return data;
    },
    columns,
    enableRowSelection: true,
  });
</script>

<div class="space-y-4">
  <DataTableToolbar {table} />
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
