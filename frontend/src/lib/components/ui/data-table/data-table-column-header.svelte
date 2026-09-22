<script lang="ts" generics="TData, TValue">
  import { Button } from '$lib/components/ui/button';
  import ArrowDownIcon from '@lucide/svelte/icons/arrow-down';
  import ArrowUpIcon from '@lucide/svelte/icons/arrow-up';
  import ChevronsUpDownIcon from '@lucide/svelte/icons/chevrons-up-down';
  import type { Column } from '@tanstack/table-core';
  import type { HTMLAttributes } from 'svelte/elements';

  let {
    column,
    title,
    class: className,
    ...restProps
  }: { column: Column<TData, TValue>; title: string } & HTMLAttributes<HTMLDivElement> = $props();
</script>

<div class={className} {...restProps}>
  {#if column?.getCanSort()}
    <Button
      variant="ghost"
      size="sm"
      class="-ms-3 h-8 data-[state=open]:bg-accent"
      onclick={column.getToggleSortingHandler()}
    >
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
