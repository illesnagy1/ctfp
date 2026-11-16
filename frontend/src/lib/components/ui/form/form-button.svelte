<script lang="ts">
  import { Button, type ButtonProps } from '#lib/components/ui/button/index.js';
  import { Spinner } from '#lib/components/ui/spinner/index.js';

  let {
    ref = $bindable(null),
    pending = false,
    children,
    ...restProps
  }: ButtonProps & {
    /** Shows a spinner, e.g. superforms' `$delayed`, which turns on only when the response is slow. */
    pending?: boolean;
  } = $props();
</script>

<Button bind:ref type="submit" {...restProps}>
  {#if pending}<Spinner />{/if}
  {@render children?.()}
</Button>
