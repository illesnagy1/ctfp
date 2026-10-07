<script lang="ts" generics="TData extends RowData, TValue extends CellData">
  import ArrowDownIcon from '@lucide/svelte/icons/arrow-down';
  import ArrowUpIcon from '@lucide/svelte/icons/arrow-up';
  import ChevronsUpDownIcon from '@lucide/svelte/icons/chevrons-up-down';
  import type { CellData, Column, RowData } from '@tanstack/svelte-table';
  import type { HTMLAttributes } from 'svelte/elements';

  import { Button } from '#lib/components/ui/button/index.js';

  import type { TasksTableFeatures } from './data-table-features.js';

  let {
    column,
    title,
    class: className,
    ...restProps
  }: { column: Column<TasksTableFeatures, TData, TValue>; title: string } & HTMLAttributes<HTMLDivElement> = $props();
</script>

<div class={className} {...restProps}>
  {#if column.getCanSort()}
    <Button variant="ghost" size="sm" class="-ms-3" onclick={column.getToggleSortingHandler()}>
      <span>
        {title}
      </span>
      {#if column.getIsSorted() === 'desc'}
        <ArrowDownIcon />
      {:else if column.getIsSorted() === 'asc'}
        <ArrowUpIcon />
      {:else}
        <ChevronsUpDownIcon />
      {/if}
    </Button>
  {:else}
    {title}
  {/if}
</div>
