<script lang="ts">
  import { enhance } from '$app/forms';
  import { resolve } from '$app/paths';
  import EllipsisIcon from '@lucide/svelte/icons/ellipsis';

  import { Button } from '#lib/components/ui/button/index.js';
  import * as DropdownMenu from '#lib/components/ui/dropdown-menu/index.js';

  let { id, banned }: { id: number; banned: boolean } = $props();

  let banForm = $state<HTMLFormElement>();
</script>

<form bind:this={banForm} method="post" action="?/setBanned" use:enhance class="hidden">
  <input type="hidden" name="id" value={id} />
  <input type="hidden" name="banned" value={String(!banned)} />
</form>

<DropdownMenu.Root>
  <DropdownMenu.Trigger>
    {#snippet child({ props })}
      <Button {...props} variant="ghost" size="icon-sm">
        <span class="sr-only">Open menu</span>
        <EllipsisIcon />
      </Button>
    {/snippet}
  </DropdownMenu.Trigger>
  <DropdownMenu.Content>
    <DropdownMenu.Item>
      {#snippet child({ props })}
        <a {...props} href={resolve('/admin/users/[id]', { id: String(id) })}>Details</a>
      {/snippet}
    </DropdownMenu.Item>
    <DropdownMenu.Item onSelect={() => banForm?.requestSubmit()}>{banned ? 'Unban' : 'Ban'}</DropdownMenu.Item>
  </DropdownMenu.Content>
</DropdownMenu.Root>
