<script lang="ts" generics="TData, TValue">
	import { Button } from "$lib/components/ui/button";
	import type { Column } from "@tanstack/table-core";
	import type { HTMLAttributes } from "svelte/elements";
	import ArrowUpIcon from "@lucide/svelte/icons/arrow-up";
	import ArrowDownIcon from "@lucide/svelte/icons/arrow-down";
	import ChevronsUpDownIcon from "@lucide/svelte/icons/chevrons-up-down";

	let {
		column,
		title,
		class: className,
		...restProps
	}: { column: Column<TData, TValue>; title: string } & HTMLAttributes<HTMLDivElement> = $props();
</script>

<div class={className} {...restProps}>
	{#if column?.getCanSort()}
		<Button
			variant="ghost"
			size="sm"
			class="data-[state=open]:bg-accent -ms-3 h-8"
			onclick={column.getToggleSortingHandler()}
		>
			<span>
				{title}
			</span>
			{#if column.getIsSorted() === "desc"}
				<ArrowDownIcon />
			{:else if column.getIsSorted() === "asc"}
				<ArrowUpIcon />
			{:else}
				<ChevronsUpDownIcon />
			{/if}
		</Button>
	{:else}
		{title}
	{/if}
</div>
