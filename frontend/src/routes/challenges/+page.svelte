<script lang="ts">
  import Trash2Icon from '@lucide/svelte/icons/trash-2';
  import { toast } from 'svelte-sonner';

  import type { ChallengeResponse } from '#lib/api/index.js';
  import { DataTable } from '#lib/components/data-table/index.js';
  import { Button } from '#lib/components/ui/button/index.js';
  import { confirmDelete } from '#lib/components/ui/confirm-delete-dialog/index.js';
  import { Progress } from '#lib/components/ui/progress/index.js';
  import { submitAction } from '#lib/form-action.js';
  import { m } from '#lib/paraglide/messages.js';

  import { adminColumns, columns } from './columns.js';
  import CreateChallengeDialog from './create-challenge-dialog.svelte';
  import DeleteCascadeWarning from './delete-cascade-warning.svelte';

  let { data } = $props();

  const isAdmin = $derived(data.user?.role === 'ADMIN');
  const solvedCount = $derived(data.challenges.filter((c) => c.solved).length);

  // Filter options are whatever categories the challenges actually use.
  const categories = $derived(
    [...new Set(data.challenges.flatMap((c) => c.categories))].toSorted().map((c) => ({ value: c, label: c })),
  );

  // Read by the details snippet while the dialog is open.
  let toDelete = $state<ChallengeResponse[]>([]);

  function askToDeleteMany(selected: ChallengeResponse[]) {
    toDelete = selected;
    confirmDelete({
      title: m.challenges_delete_many_title({ count: selected.length }),
      description: m.challenges_delete_many_description({ count: selected.length }),
      details: deleteManyDetails,
      input: { confirmationText: m.challenges_delete_many_keyword() },
      onConfirm: async () => {
        await submitAction('?/deleteMany', { ids: selected.map((c) => c.id).join(',') });
        toast.success(m.challenges_deleted_many({ count: selected.length }));
      },
    });
  }
</script>

{#snippet deleteManyDetails()}
  <ul class="max-h-32 list-disc overflow-y-auto pl-5 text-sm">
    {#each toDelete as challenge (challenge.id)}
      <li>{challenge.title}</li>
    {/each}
  </ul>
  <DeleteCascadeWarning />
{/snippet}

{#snippet bulkActions(selected: ChallengeResponse[])}
  {#if selected.length > 0}
    <Button variant="destructive" size="sm" onclick={() => askToDeleteMany(selected)}>
      <Trash2Icon />
      {m.challenges_delete_selected({ count: selected.length })}
    </Button>
  {/if}
{/snippet}

<div class="mx-auto w-full max-w-6xl px-4 py-6">
  <div class="flex items-center justify-between gap-4 pb-4">
    <div class="flex flex-col gap-1">
      <h1 class="text-2xl font-semibold tracking-tight">{m.challenges_title()}</h1>
      {#if data.user && !isAdmin && data.challenges.length > 0}
        <div class="flex items-center gap-2 text-sm text-muted-foreground">
          <Progress value={solvedCount} max={data.challenges.length} class="w-32" aria-hidden="true" />
          <span class="tabular-nums">
            {m.challenges_progress({ solved: solvedCount, total: data.challenges.length })}
          </span>
        </div>
      {/if}
    </div>
    {#if isAdmin}
      <CreateChallengeDialog data={data.createForm} knownCategories={categories.map((c) => c.value)} />
    {/if}
  </div>
  <DataTable
    data={data.challenges}
    columns={isAdmin ? adminColumns : columns}
    selectable={isAdmin}
    getRowId={(challenge) => String(challenge.id)}
    isHighlighted={(challenge) => !!challenge.solved}
    actions={isAdmin ? bulkActions : undefined}
    search={{ placeholder: m.challenges_filter_placeholder() }}
    filters={[{ column: 'categories', title: m.challenges_column_categories(), options: categories }]}
  />
</div>
