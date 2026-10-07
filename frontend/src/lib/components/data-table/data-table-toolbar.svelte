<script lang="ts" generics="TData extends RowData">
  import XIcon from '@lucide/svelte/icons/x';
  import type { RowData, SvelteTable } from '@tanstack/svelte-table';

  import { Button } from '#lib/components/ui/button/index.js';
  import { Input } from '#lib/components/ui/input/index.js';

  import DataTableFacetedFilter from './data-table-faceted-filter.svelte';
  import DataTableViewOptions from './data-table-view-options.svelte';
  import type { DataTableFeatures } from './features.js';
  import type { DataTableFilter, DataTableSearch } from './types.js';

  let {
    table,
    search,
    filters = [],
  }: {
    table: SvelteTable<DataTableFeatures, TData>;
    search?: DataTableSearch;
    filters?: DataTableFilter[];
  } = $props();

  const isFiltered = $derived(table.atoms.columnFilters.get().length > 0);
  const searchColumn = $derived(search && table.getColumn(search.column));
</script>

<div class="flex items-center justify-between">
  <div class="flex flex-1 items-center space-x-2">
    {#if search && searchColumn}
      <Input
        placeholder={search.placeholder}
        value={(searchColumn.getFilterValue() as string | undefined) ?? ''}
        oninput={(e) => searchColumn.setFilterValue(e.currentTarget.value)}
        class="h-8 w-37.5 lg:w-62.5"
      />
    {/if}

    {#each filters as filter (filter.column)}
      {@const column = table.getColumn(filter.column)}
      {#if column}
        <DataTableFacetedFilter {column} title={filter.title} options={filter.options} />
      {/if}
    {/each}

    {#if isFiltered}
      <Button variant="ghost" size="sm" onclick={() => table.resetColumnFilters()}>
        Reset
        <XIcon />
      </Button>
    {/if}
  </div>
  <DataTableViewOptions {table} />
</div>
