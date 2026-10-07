<script lang="ts">
  import { DataTable } from '#lib/components/data-table/index.js';
  import { roleOptions } from '#lib/components/users/labels.js';
  import { m } from '#lib/paraglide/messages.js';

  import { columns } from './columns.js';

  let { data, form } = $props();
</script>

<div class="mx-auto w-full max-w-6xl px-4 py-6">
  {#if form?.message}
    <p class="pb-4 text-sm text-destructive">{form.message}</p>
  {/if}
  <DataTable
    data={data.users}
    {columns}
    search={{ column: 'username', placeholder: m.users_filter_placeholder() }}
    filters={[{ column: 'role', title: m.users_column_role(), options: roleOptions() }]}
  />
</div>
