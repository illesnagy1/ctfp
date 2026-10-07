<script lang="ts">
  import { enhance } from '$app/forms';
  import { resolve } from '$app/paths';

  import { DataTableRowMenu } from '#lib/components/data-table/index.js';
  import * as DropdownMenu from '#lib/components/ui/dropdown-menu/index.js';
  import { m } from '#lib/paraglide/messages.js';

  let { id, banned }: { id: number; banned: boolean } = $props();

  let banForm = $state<HTMLFormElement>();
</script>

<form bind:this={banForm} method="post" action="?/setBanned" use:enhance class="hidden">
  <input type="hidden" name="id" value={id} />
  <input type="hidden" name="banned" value={String(!banned)} />
</form>

<DataTableRowMenu>
  <DropdownMenu.Item>
    {#snippet child({ props })}
      <a {...props} href={resolve('/admin/users/[id]', { id: String(id) })}>{m.users_action_details()}</a>
    {/snippet}
  </DropdownMenu.Item>
  <DropdownMenu.Item onSelect={() => banForm?.requestSubmit()}>
    {banned ? m.users_action_unban() : m.users_action_ban()}
  </DropdownMenu.Item>
</DataTableRowMenu>
