<script lang="ts">
  import { resolve } from '$app/paths';

  import { DataTableRowMenu } from '#lib/components/data-table/index.js';
  import * as DropdownMenu from '#lib/components/ui/dropdown-menu/index.js';
  import { m } from '#lib/paraglide/messages.js';

  let { id }: { id: number } = $props();
</script>

<DataTableRowMenu>
  <DropdownMenu.Item>
    {#snippet child({ props })}
      <a {...props} href={resolve('/challenges/[id]', { id: String(id) })}>{m.challenges_action_details()}</a>
    {/snippet}
  </DropdownMenu.Item>
</DataTableRowMenu>
