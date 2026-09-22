<script lang="ts">
	import type { ComponentProps } from 'svelte';
	import ArrowUpDownIcon from '@lucide/svelte/icons/arrow-up-down';
	import { Button } from '$lib/components/ui/button';

	type SortHeaderProps = ComponentProps<typeof Button> & { label: string };

	let { label, variant = 'ghost', ...restProps }: SortHeaderProps = $props();
</script>

<Button {variant} {...restProps}>
	{label}
	<ArrowUpDownIcon class="ms-2 size-4" />
</Button>
