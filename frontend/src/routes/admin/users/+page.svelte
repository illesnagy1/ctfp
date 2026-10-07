<script lang="ts">
  import DataTable from '#lib/components/data-table.svelte';

  import { columns } from './columns.js';

  let { data, form } = $props();
</script>

<div class="mx-auto w-full max-w-6xl px-4 py-6">
  {#if form?.message}
    <p class="pb-4 text-sm text-destructive">{form.message}</p>
  {/if}
  <DataTable data={data.users} {columns} />
</div>
