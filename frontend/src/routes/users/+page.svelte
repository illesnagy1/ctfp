<script lang="ts">
  import { DataTable } from '#lib/components/data-table/index.js';

  import { columns } from './columns.js';

  let { data } = $props();
</script>

<div class="mx-auto w-full max-w-6xl px-4 py-6">
  <DataTable data={data.users} {columns} search={{ column: 'username', placeholder: 'Filter usernames...' }} />
</div>
