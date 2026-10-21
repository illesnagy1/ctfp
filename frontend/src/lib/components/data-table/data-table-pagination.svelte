<script lang="ts" generics="TData extends RowData">
  import ChevronLeftIcon from '@lucide/svelte/icons/chevron-left';
  import ChevronRightIcon from '@lucide/svelte/icons/chevron-right';
  import ChevronsLeftIcon from '@lucide/svelte/icons/chevrons-left';
  import ChevronsRightIcon from '@lucide/svelte/icons/chevrons-right';
  import type { RowData, SvelteTable } from '@tanstack/svelte-table';

  import { Button } from '#lib/components/ui/button/index.js';
  import * as Select from '#lib/components/ui/select/index.js';
  import { m } from '#lib/paraglide/messages.js';

  import type { DataTableFeatures } from './features.js';

  let {
    table,
    showSelection = true,
  }: {
    table: SvelteTable<DataTableFeatures, TData>;
    /** Shows the "n of m row(s) selected" count; off for tables without selection. */
    showSelection?: boolean;
  } = $props();

  const pageCount = $derived(table.getPageCount());
  const selectedRows = $derived(table.getFilteredSelectedRowModel().rows.length);
  const totalRows = $derived(table.getFilteredRowModel().rows.length);
  const pagination = $derived(table.atoms.pagination.get());
  const pageSize = $derived(pagination.pageSize);
  const pageIndex = $derived(pagination.pageIndex);
</script>

<div class="flex items-center justify-between px-2">
  <div class="flex-1 text-sm text-muted-foreground">
    {#if showSelection}
      {m.data_table_selected_rows({ count: selectedRows, total: totalRows })}
    {/if}
  </div>
  <div class="flex items-center space-x-6 lg:space-x-8">
    <div class="flex items-center space-x-2">
      <p class="text-sm font-medium">{m.data_table_rows_per_page()}</p>
      <Select.Root
        allowDeselect={false}
        type="single"
        value={pageSize.toString()}
        onValueChange={(value) => {
          table.setPageSize(Number(value));
        }}
      >
        <Select.Trigger class="h-8 w-17.5">
          {pageSize.toString()}
        </Select.Trigger>
        <Select.Content side="top">
          {#each [10, 20, 30, 40, 50] as size (size)}
            <Select.Item value={size.toString()}>
              {size}
            </Select.Item>
          {/each}
        </Select.Content>
      </Select.Root>
    </div>
    <div class="flex w-25 items-center justify-center text-sm font-medium">
      {m.data_table_page({ page: pageIndex + 1, pages: pageCount })}
    </div>
    <div class="flex items-center space-x-2">
      <Button
        variant="outline"
        size="icon-sm"
        class="hidden lg:flex"
        onclick={() => table.setPageIndex(0)}
        disabled={!table.getCanPreviousPage()}
      >
        <span class="sr-only">{m.data_table_first_page()}</span>
        <ChevronsLeftIcon />
      </Button>
      <Button
        variant="outline"
        size="icon-sm"
        onclick={() => table.previousPage()}
        disabled={!table.getCanPreviousPage()}
      >
        <span class="sr-only">{m.data_table_previous_page()}</span>
        <ChevronLeftIcon />
      </Button>
      <Button variant="outline" size="icon-sm" onclick={() => table.nextPage()} disabled={!table.getCanNextPage()}>
        <span class="sr-only">{m.data_table_next_page()}</span>
        <ChevronRightIcon />
      </Button>
      <Button
        variant="outline"
        size="icon-sm"
        class="hidden lg:flex"
        onclick={() => table.setPageIndex(pageCount - 1)}
        disabled={!table.getCanNextPage()}
      >
        <span class="sr-only">{m.data_table_last_page()}</span>
        <ChevronsRightIcon />
      </Button>
    </div>
  </div>
</div>
