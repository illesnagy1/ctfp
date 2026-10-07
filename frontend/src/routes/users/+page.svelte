<script lang="ts">
  import { isAdminUser } from '$lib/types/user';

  import { adminColumns, columns } from './columns.js';
  import DataTable from './data-table.svelte';

  let { data, form } = $props();

  let isAdmin = $derived(data.user?.role === 'ADMIN');
</script>

<div class="mx-auto w-full max-w-6xl px-4 py-6">
  {#if form?.message}
    <p class="pb-4 text-sm text-destructive">{form.message}</p>
  {/if}
  {#if isAdmin}
    <DataTable data={data.users.filter(isAdminUser)} columns={adminColumns} />
  {:else}
    <DataTable data={data.users} {columns} />
  {/if}
</div>
