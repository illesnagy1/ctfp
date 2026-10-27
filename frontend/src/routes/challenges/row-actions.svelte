<script lang="ts">
  import { toast } from 'svelte-sonner';

  import { DataTableRowMenu } from '#lib/components/data-table/index.js';
  import { confirmDelete } from '#lib/components/ui/confirm-delete-dialog/index.js';
  import * as DropdownMenu from '#lib/components/ui/dropdown-menu/index.js';
  import { submitAction } from '#lib/form-action.js';
  import { m } from '#lib/paraglide/messages.js';

  import DeleteCascadeWarning from './delete-cascade-warning.svelte';

  let { id, title }: { id: number; title: string } = $props();

  function askToDelete() {
    confirmDelete({
      title: m.challenges_delete_title(),
      description: m.challenges_delete_description({ title }),
      details: cascadeWarning,
      // Deleting also wipes flags and solves, so make it deliberate.
      input: { confirmationText: title },
      onConfirm: async () => {
        await submitAction('?/delete', { id: String(id) });
        toast.success(m.challenges_deleted({ title }));
      },
    });
  }
</script>

{#snippet cascadeWarning()}
  <DeleteCascadeWarning />
{/snippet}

<!-- Admins only (see adminColumns); the delete action and the api check the role again. -->
<DataTableRowMenu>
  <DropdownMenu.Item variant="destructive" onSelect={askToDelete}>
    {m.challenges_action_delete()}
  </DropdownMenu.Item>
</DataTableRowMenu>
