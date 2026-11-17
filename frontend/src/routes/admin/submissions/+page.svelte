<script lang="ts">
  import { DataTable } from '#lib/components/data-table/index.js';
  import { adminColumns, resultOptions } from '#lib/components/submissions/columns.js';
  import { m } from '#lib/paraglide/messages.js';

  let { data } = $props();
</script>

<div class="mx-auto w-full max-w-6xl px-4 py-6">
  <h1 class="pb-4 text-2xl font-semibold tracking-tight">{m.submissions_title()}</h1>
  <DataTable
    data={data.submissions}
    columns={adminColumns}
    selectable={false}
    getRowId={(submission) => String(submission.id)}
    search={{ placeholder: m.submissions_filter_placeholder() }}
    filters={[{ column: 'result', title: m.submissions_column_result(), options: resultOptions() }]}
  />
</div>
