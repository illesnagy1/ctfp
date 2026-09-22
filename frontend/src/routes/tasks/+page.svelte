<script lang="ts">
	import DataTable from './components/data-table.svelte';
	import { data } from './data/tasks.js';
	import { columns } from './components/columns.js';
</script>

<div class="hidden h-full flex-1 flex-col gap-8 p-8 md:flex">
	<div class="flex items-center justify-between gap-2">
		<div class="flex flex-col gap-1">
			<h2 class="text-2xl font-semibold tracking-tight">Welcome back!</h2>
			<p class="text-muted-foreground">Here&apos;s a list of your tasks for this month.</p>
		</div>
	</div>
	<DataTable {data} {columns} />
</div>
