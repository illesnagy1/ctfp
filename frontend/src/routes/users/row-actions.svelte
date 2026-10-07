<script lang="ts">
  import { resolve } from '$app/paths';

  import { DataTableRowMenu } from '#lib/components/data-table/index.js';
  import * as DropdownMenu from '#lib/components/ui/dropdown-menu/index.js';

  let { id }: { id: number } = $props();
</script>

<DataTableRowMenu>
  <DropdownMenu.Item>
    {#snippet child({ props })}
      <a {...props} href={resolve('/users/[id]', { id: String(id) })}>Details</a>
    {/snippet}
  </DropdownMenu.Item>
</DataTableRowMenu>
