<script lang="ts">
  import { enhance } from '$app/forms';
  import { resolve } from '$app/paths';
  import { page } from '$app/state';
  import { toast } from 'svelte-sonner';
  import { defaults } from 'sveltekit-superforms';
  import { zod4Client } from 'sveltekit-superforms/adapters';

  import { DataTableRowMenu } from '#lib/components/data-table/index.js';
  import * as DropdownMenu from '#lib/components/ui/dropdown-menu/index.js';
  import EditUserDialog from '#lib/components/users/edit-user-dialog.svelte';
  import { actionError } from '#lib/form-action.js';
  import { m } from '#lib/paraglide/messages.js';
  import { editUserData, editUserFormId, editUserSchema } from '#lib/schemas/user.js';
  import type { AdminUser } from '#lib/types/user.js';

  let { user }: { user: AdminUser } = $props();

  // Banning yourself would revoke your own admin rights; the api refuses it too.
  const isSelf = $derived(page.data.user?.id === user.id);
  const banned = $derived(user.isBanned ?? false);

  let banForm = $state<HTMLFormElement>();

  // The edit dialog is mounted on demand, so a long table doesn't build a form per row, and fresh each time from the
  // row's data: the saved profile, reloaded after each save. It saves through the user page's action.
  let editing = $state(false);
  let edits = $state(0);
  const editData = $derived(defaults(editUserData(user), zod4Client(editUserSchema), { id: editUserFormId(user.id) }));

  function edit() {
    edits += 1;
    editing = true;
  }
</script>

<form
  bind:this={banForm}
  method="post"
  action="?/setBanned"
  class="hidden"
  use:enhance={() => {
    // Captured now: after the reload `banned` already shows the new state.
    const banning = !banned;
    const { username } = user;
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
  <input type="hidden" name="id" value={user.id} />
  <input type="hidden" name="banned" value={String(!banned)} />
</form>

<!-- Admins only (see adminColumns). -->
<DataTableRowMenu>
  <DropdownMenu.Item onSelect={edit}>{m.users_action_edit()}</DropdownMenu.Item>
  {#if !isSelf}
    <DropdownMenu.Item variant={banned ? 'default' : 'destructive'} onSelect={() => banForm?.requestSubmit()}>
      {banned ? m.users_action_unban() : m.users_action_ban()}
    </DropdownMenu.Item>
  {/if}
</DataTableRowMenu>

{#key edits}
  {#if edits > 0}
    <EditUserDialog
      data={editData}
      admin
      self={isSelf}
      action={`${resolve('/users/[id]', { id: String(user.id) })}?/update`}
      trigger={false}
      bind:open={editing}
    />
  {/if}
{/key}
