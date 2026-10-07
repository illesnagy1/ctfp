<script lang="ts">
  import EllipsisIcon from '@lucide/svelte/icons/ellipsis';
  import type { Row } from '@tanstack/svelte-table';

  import { Button } from '#lib/components/ui/button/index.js';
  import * as DropdownMenu from '#lib/components/ui/dropdown-menu/index.js';

  import { labels } from '../data/data.js';
  import { taskSchema, type Task } from '../data/schemas.js';
  import type { TasksTableFeatures } from './data-table-features.js';

  let { row }: { row: Row<TasksTableFeatures, Task> } = $props();

  const task = $derived(taskSchema.parse(row.original));
</script>

<DropdownMenu.Root>
  <DropdownMenu.Trigger>
    {#snippet child({ props })}
      <Button {...props} variant="ghost" size="icon-sm">
        <EllipsisIcon />
        <span class="sr-only">Open Menu</span>
      </Button>
    {/snippet}
  </DropdownMenu.Trigger>
  <DropdownMenu.Content class="w-40" align="end">
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
  </DropdownMenu.Content>
</DropdownMenu.Root>
