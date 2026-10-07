<script lang="ts">
  import EllipsisIcon from '@lucide/svelte/icons/ellipsis';
  import type { Snippet } from 'svelte';

  import { Button } from '#lib/components/ui/button/index.js';
  import * as DropdownMenu from '#lib/components/ui/dropdown-menu/index.js';
  import { m } from '#lib/paraglide/messages.js';

  /** The "⋯" menu at the end of a row; `children` are the `DropdownMenu` items. */
  let { children }: { children: Snippet } = $props();
</script>

<DropdownMenu.Root>
  <DropdownMenu.Trigger>
    {#snippet child({ props })}
      <Button {...props} variant="ghost" size="icon-sm">
        <span class="sr-only">{m.data_table_open_menu()}</span>
        <EllipsisIcon />
      </Button>
    {/snippet}
  </DropdownMenu.Trigger>
  <DropdownMenu.Content class="w-40" align="end">
    {@render children()}
  </DropdownMenu.Content>
</DropdownMenu.Root>
