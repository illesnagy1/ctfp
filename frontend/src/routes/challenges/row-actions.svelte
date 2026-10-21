<script lang="ts">
  import { resolve } from '$app/paths';
  import { page } from '$app/state';

  import { DataTableRowMenu } from '#lib/components/data-table/index.js';
  import { confirmDelete } from '#lib/components/ui/confirm-delete-dialog/index.js';
  import * as DropdownMenu from '#lib/components/ui/dropdown-menu/index.js';
  import { submitAction } from '#lib/form-action.js';
  import { m } from '#lib/paraglide/messages.js';

  import DeleteCascadeWarning from './delete-cascade-warning.svelte';

  let { id, title }: { id: number; title: string } = $props();

  // Only hides the option; the delete action and the api check the role again.
  const isAdmin = $derived(page.data.user?.role === 'ADMIN');

  function askToDelete() {
    confirmDelete({
      title: m.challenges_delete_title(),
      description: m.challenges_delete_description({ title }),
      details: cascadeWarning,
      // Deleting also wipes flags and solves, so make it deliberate.
      input: { confirmationText: title },
      onConfirm: () => submitAction('?/delete', { id: String(id) }),
    });
  }
</script>

{#snippet cascadeWarning()}
  <DeleteCascadeWarning />
{/snippet}

<DataTableRowMenu>
  <DropdownMenu.Item>
    {#snippet child({ props })}
      <a {...props} href={resolve('/challenges/[id]', { id: String(id) })}>{m.challenges_action_details()}</a>
    {/snippet}
  </DropdownMenu.Item>
  {#if isAdmin}
    <DropdownMenu.Separator />
    <DropdownMenu.Item variant="destructive" onSelect={askToDelete}>
      {m.challenges_action_delete()}
    </DropdownMenu.Item>
  {/if}
</DataTableRowMenu>
