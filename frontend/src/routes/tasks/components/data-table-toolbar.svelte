<script lang="ts" generics="TData extends RowData">
  import XIcon from '@lucide/svelte/icons/x';
  import type { RowData, SvelteTable } from '@tanstack/svelte-table';

  import Button from '#lib/components/ui/button/button.svelte';
  import { Input } from '#lib/components/ui/input/index.js';

  import { priorities, statuses } from '../data/data.js';
  import DataTableFacetedFilter from './data-table-faceted-filter.svelte';
  import type { TasksTableFeatures } from './data-table-features.js';
  import DataTableViewOptions from './data-table-view-options.svelte';

  let { table }: { table: SvelteTable<TasksTableFeatures, TData> } = $props();

  const isFiltered = $derived(table.atoms.columnFilters.get().length > 0);
  const statusCol = $derived(table.getColumn('status'));
  const priorityCol = $derived(table.getColumn('priority'));
</script>

<div class="flex items-center justify-between">
  <div class="flex flex-1 items-center space-x-2">
    <Input
      placeholder="Filter tasks..."
      value={(table.getColumn('title')?.getFilterValue() as string) ?? ''}
      oninput={(e) => {
        table.getColumn('title')?.setFilterValue(e.currentTarget.value);
      }}
      onchange={(e) => {
        table.getColumn('title')?.setFilterValue(e.currentTarget.value);
      }}
      class="h-8 w-37.5 lg:w-62.5"
    />

    {#if statusCol}
      <DataTableFacetedFilter column={statusCol} title="Status" options={statuses} />
    {/if}
    {#if priorityCol}
      <DataTableFacetedFilter column={priorityCol} title="Priority" options={priorities} />
    {/if}

    {#if isFiltered}
      <Button variant="ghost" size="sm" onclick={() => table.resetColumnFilters()}>
        Reset
        <XIcon />
      </Button>
    {/if}
  </div>
  <DataTableViewOptions {table} />
</div>
