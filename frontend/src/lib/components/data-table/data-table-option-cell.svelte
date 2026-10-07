<script lang="ts">
  import type { HTMLAttributes } from 'svelte/elements';

  import type { DataTableOption } from './types.js';

  /** Shows the option matching `value` with its icon, e.g. a status. Renders nothing for an unknown value. */
  let { value, options, ...restProps }: { value: string; options: DataTableOption[] } & HTMLAttributes<HTMLDivElement> =
    $props();

  const option = $derived(options.find((o) => o.value === value));
</script>

{#if option}
  <div {...restProps} class={['flex items-center', restProps.class]}>
    {#if option.icon}
      <option.icon class="me-2 size-4 text-muted-foreground" />
    {/if}
    <span>{option.label}</span>
  </div>
{/if}
