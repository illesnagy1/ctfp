<script lang="ts">
  import { enhance } from '$app/forms';
  import { resolve } from '$app/paths';
  import { page } from '$app/state';
  import { toast } from 'svelte-sonner';

  import { DataTableRowMenu } from '#lib/components/data-table/index.js';
  import * as DropdownMenu from '#lib/components/ui/dropdown-menu/index.js';
  import { actionError } from '#lib/form-action.js';
  import { m } from '#lib/paraglide/messages.js';

  let { id, username, banned }: { id: number; username: string; banned: boolean } = $props();

  // Banning yourself would revoke your own admin rights; the action refuses it too.
  const isSelf = $derived(page.data.user?.id === id);

  let banForm = $state<HTMLFormElement>();
</script>

<form
  bind:this={banForm}
  method="post"
  action="?/setBanned"
  class="hidden"
  use:enhance={() => {
    // Captured now: after the reload `banned` already shows the new state.
    const banning = !banned;
    return async ({ result, update }) => {
      const error = actionError(result);
      if (error) {
        toast.error(error);
        return;
      }
      await update();
      if (result.type === 'success') {
        toast.success(banning ? m.users_banned({ username }) : m.users_unbanned({ username }));
      }
    };
  }}
>
  <input type="hidden" name="id" value={id} />
  <input type="hidden" name="banned" value={String(!banned)} />
</form>

<DataTableRowMenu>
  <DropdownMenu.Item>
    {#snippet child({ props })}
      <a {...props} href={resolve('/admin/users/[id]', { id: String(id) })}>{m.users_action_details()}</a>
    {/snippet}
  </DropdownMenu.Item>
  {#if !isSelf}
    <DropdownMenu.Item variant={banned ? 'default' : 'destructive'} onSelect={() => banForm?.requestSubmit()}>
      {banned ? m.users_action_unban() : m.users_action_ban()}
    </DropdownMenu.Item>
  {/if}
</DataTableRowMenu>
