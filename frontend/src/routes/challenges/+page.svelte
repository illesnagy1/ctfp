<script lang="ts">
  import { DataTable } from '#lib/components/data-table/index.js';
  import { m } from '#lib/paraglide/messages.js';

  import { columns } from './columns.js';

  let { data } = $props();

  // Filter options are whatever categories the challenges actually use.
  const categories = $derived(
    [...new Set(data.challenges.flatMap((c) => c.categories))].toSorted().map((c) => ({ value: c, label: c })),
  );
</script>

<div class="mx-auto w-full max-w-6xl px-4 py-6">
  <h1 class="pb-4 text-2xl font-semibold tracking-tight">{m.challenges_title()}</h1>
  <DataTable
    data={data.challenges}
    {columns}
    search={{ column: 'title', placeholder: m.challenges_filter_placeholder() }}
    filters={[{ column: 'categories', title: m.challenges_column_categories(), options: categories }]}
  />
</div>
