<script lang="ts" generics="TData extends RowData">
  import Settings2Icon from '@lucide/svelte/icons/settings-2';
  import type { RowData, SvelteTable } from '@tanstack/svelte-table';

  import { Button } from '#lib/components/ui/button/index.js';
  import * as DropdownMenu from '#lib/components/ui/dropdown-menu/index.js';
  import { m } from '#lib/paraglide/messages.js';

  import type { DataTableFeatures } from './features.js';
  import { resolveText } from './types.js';

  let { table }: { table: SvelteTable<DataTableFeatures, TData> } = $props();
</script>

<DropdownMenu.Root>
  <DropdownMenu.Trigger>
    {#snippet child({ props })}
      <Button {...props} variant="outline" size="sm" class="ms-auto hidden lg:flex">
        <Settings2Icon />
        {m.data_table_view()}
      </Button>
    {/snippet}
  </DropdownMenu.Trigger>
  <DropdownMenu.Content>
    <DropdownMenu.Group>
      <DropdownMenu.Label>{m.data_table_toggle_columns()}</DropdownMenu.Label>
      <DropdownMenu.Separator />
      {#each table
        .getAllColumns()
        .filter((col) => typeof col.accessorFn !== 'undefined' && col.getCanHide()) as column (column.id)}
        <DropdownMenu.CheckboxItem bind:checked={() => column.getIsVisible(), (v) => column.toggleVisibility(!!v)}>
          <span>{column.columnDef.meta?.title ? resolveText(column.columnDef.meta.title) : column.id}</span>
        </DropdownMenu.CheckboxItem>
      {/each}
    </DropdownMenu.Group>
  </DropdownMenu.Content>
</DropdownMenu.Root>
