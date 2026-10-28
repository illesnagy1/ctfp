<script lang="ts">
  import type { HTMLAttributes } from 'svelte/elements';

  import { cn, type WithElementRef } from '#lib/utils.js';

  let {
    ref = $bindable(null),
    class: className,
    children,
    ...restProps
  }: WithElementRef<HTMLAttributes<HTMLTableRowElement>> = $props();
</script>

<tr
  bind:this={ref}
  data-slot="table-row"
  class={cn(
    // `data-highlighted` marks rows to stand out positively, e.g. solved challenges.
    'border-b transition-colors hover:bg-muted/50 has-aria-expanded:bg-muted/50 data-highlighted:bg-success data-[state=selected]:bg-muted',
    className,
  )}
  {...restProps}
>
  {@render children?.()}
</tr>
