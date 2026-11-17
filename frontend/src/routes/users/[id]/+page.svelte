<script lang="ts">
  import { DataTable } from '#lib/components/data-table/index.js';
  import { columns, resultOptions } from '#lib/components/submissions/columns.js';
  import EditUserDialog from '#lib/components/users/edit-user-dialog.svelte';
  import UserProfile from '#lib/components/users/user-profile.svelte';
  import { m } from '#lib/paraglide/messages.js';

  let { data } = $props();
</script>

<!-- Only for your own profile, or any as an admin. -->
{#snippet edit()}
  {#if data.editForm}
    <EditUserDialog data={data.editForm} admin={data.admin} self={data.self} />
  {/if}
{/snippet}

<div class="mx-auto max-w-2xl px-4 py-12">
  <UserProfile user={data.profile} actions={data.editForm ? edit : undefined} />
</div>

{#if data.submissions}
  <section class="mx-auto w-full max-w-6xl px-4 pb-12">
    <h2 class="pb-4 text-xl font-semibold tracking-tight">
      {data.self ? m.submissions_mine_title() : m.submissions_title()}
    </h2>
    <DataTable
      data={data.submissions}
      {columns}
      selectable={false}
      getRowId={(submission) => String(submission.id)}
      search={{ placeholder: m.submissions_filter_placeholder() }}
      filters={[{ column: 'result', title: m.submissions_column_result(), options: resultOptions() }]}
    />
  </section>
{/if}
