<script lang="ts">
  import { resolve } from '$app/paths';
  import { page } from '$app/state';
  import TriangleAlertIcon from '@lucide/svelte/icons/triangle-alert';

  import { DataTableRowMenu } from '#lib/components/data-table/index.js';
  import { confirmDelete } from '#lib/components/ui/confirm-delete-dialog/index.js';
  import * as DropdownMenu from '#lib/components/ui/dropdown-menu/index.js';
  import { submitAction } from '#lib/form-action.js';
  import { m } from '#lib/paraglide/messages.js';

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
  <!-- Mirrors the api's ON DELETE CASCADE on flags, hints (with unlocks) and submissions. -->
  <div role="note" class="flex gap-3 rounded-md border border-destructive/50 bg-destructive/10 p-3 text-sm">
    <TriangleAlertIcon class="mt-0.5 size-4 shrink-0 text-destructive" />
    <div>
      <p class="font-medium text-destructive">{m.challenges_delete_cascade_title()}</p>
      <ul class="mt-1 list-disc pl-4">
        <li>{m.challenges_delete_cascade_flags()}</li>
        <li>{m.challenges_delete_cascade_hints()}</li>
        <li>{m.challenges_delete_cascade_submissions()}</li>
      </ul>
    </div>
  </div>
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
