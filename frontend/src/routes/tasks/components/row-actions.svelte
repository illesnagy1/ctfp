<script lang="ts">
  import type { Row } from '@tanstack/svelte-table';

  import { DataTableRowMenu, type DataTableFeatures } from '#lib/components/data-table/index.js';
  import * as DropdownMenu from '#lib/components/ui/dropdown-menu/index.js';

  import { labels } from '../data/data.js';
  import { taskSchema, type Task } from '../data/schemas.js';

  let { row }: { row: Row<DataTableFeatures, Task> } = $props();

  const task = $derived(taskSchema.parse(row.original));
</script>

<DataTableRowMenu>
  <DropdownMenu.Item>Edit</DropdownMenu.Item>
  <DropdownMenu.Item>Make a copy</DropdownMenu.Item>
  <DropdownMenu.Item>Favorite</DropdownMenu.Item>
  <DropdownMenu.Separator />
  <DropdownMenu.Sub>
    <DropdownMenu.SubTrigger>Labels</DropdownMenu.SubTrigger>
    <DropdownMenu.SubContent>
      <DropdownMenu.RadioGroup value={task.label}>
        {#each labels as label (label.value)}
          <DropdownMenu.RadioItem value={label.value}>
            {label.label}
          </DropdownMenu.RadioItem>
        {/each}
      </DropdownMenu.RadioGroup>
    </DropdownMenu.SubContent>
  </DropdownMenu.Sub>
  <DropdownMenu.Separator />
  <DropdownMenu.Item>
    Delete
    <DropdownMenu.Shortcut>⌘⌫</DropdownMenu.Shortcut>
  </DropdownMenu.Item>
</DataTableRowMenu>
