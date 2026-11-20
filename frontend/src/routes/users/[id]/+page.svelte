<script lang="ts">
  import { resolve } from '$app/paths';
  import DownloadIcon from '@lucide/svelte/icons/download';

  import { DataTable } from '#lib/components/data-table/index.js';
  import { columns, resultOptions } from '#lib/components/submissions/columns.js';
  import EditUserDialog from '#lib/components/users/edit-user-dialog.svelte';
  import UserProfile from '#lib/components/users/user-profile.svelte';
  import { m } from '#lib/paraglide/messages.js';

  import DeleteAccount from './delete-account.svelte';

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
  {#if data.self && data.deleteForm}
    <div class="mt-6 flex flex-col gap-6">
      <!-- A file download, not a page: the endpoint answers with an attachment. -->
      <a
        href={resolve('/users/me/export')}
        data-sveltekit-reload
        class="flex items-center gap-2 self-start text-sm underline"
      >
        <DownloadIcon class="size-4" />
        {m.account_export()}
      </a>
      <DeleteAccount data={data.deleteForm} />
    </div>
  {/if}
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
