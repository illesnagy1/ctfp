<script lang="ts" generics="TData extends RowData">
  import XIcon from '@lucide/svelte/icons/x';
  import type { RowData, SvelteTable } from '@tanstack/svelte-table';
  import type { Snippet } from 'svelte';

  import { Button } from '#lib/components/ui/button/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import { m } from '#lib/paraglide/messages.js';

  import DataTableFacetedFilter from './data-table-faceted-filter.svelte';
  import DataTableViewOptions from './data-table-view-options.svelte';
  import type { DataTableFeatures } from './features.js';
  import type { DataTableFilter, DataTableSearch } from './types.js';

  let {
    table,
    search,
    filters = [],
    children,
  }: {
    table: SvelteTable<DataTableFeatures, TData>;
    search?: DataTableSearch;
    filters?: DataTableFilter[];
    /** Extra buttons, shown before the view options. */
    children?: Snippet;
  } = $props();

  const searchValue = $derived(String(table.atoms.globalFilter.get() ?? ''));
  const isFiltered = $derived(table.atoms.columnFilters.get().length > 0 || searchValue !== '');

  function reset() {
    table.resetColumnFilters();
    table.resetGlobalFilter(true);
  }
</script>

<div class="flex items-center justify-between">
  <div class="flex flex-1 items-center space-x-2">
    {#if search}
      <Input
        type="search"
        placeholder={search.placeholder}
        value={searchValue}
        oninput={(e) => table.setGlobalFilter(e.currentTarget.value)}
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
      <Button variant="ghost" size="sm" onclick={reset}>
        {m.data_table_reset()}
        <XIcon />
      </Button>
    {/if}
  </div>
  <div class="flex items-center gap-2">
    {@render children?.()}
    <DataTableViewOptions {table} />
  </div>
</div>
