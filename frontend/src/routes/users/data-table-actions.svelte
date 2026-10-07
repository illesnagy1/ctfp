<script lang="ts">
  import { enhance } from '$app/forms';
  import { resolve } from '$app/paths';
  import { Button } from '$lib/components/ui/button/index.js';
  import * as DropdownMenu from '$lib/components/ui/dropdown-menu/index.js';
  import EllipsisIcon from '@lucide/svelte/icons/ellipsis';

  /** `banned` is only passed for admins; without it the ban action is hidden. */
  let { id, banned }: { id: number; banned?: boolean } = $props();

  let banForm = $state<HTMLFormElement>();
</script>

{#if banned !== undefined}
  <form bind:this={banForm} method="post" action="?/setBanned" use:enhance class="hidden">
    <input type="hidden" name="id" value={id} />
    <input type="hidden" name="banned" value={String(!banned)} />
  </form>
{/if}

<DropdownMenu.Root>
  <DropdownMenu.Trigger>
    {#snippet child({ props })}
      <Button {...props} variant="ghost" size="icon" class="relative size-8 p-0">
        <span class="sr-only">Open menu</span>
        <EllipsisIcon />
      </Button>
    {/snippet}
  </DropdownMenu.Trigger>
  <DropdownMenu.Content>
    <DropdownMenu.Item>
      {#snippet child({ props })}
        <a {...props} href={resolve('/users/[id]', { id: String(id) })}>Details</a>
      {/snippet}
    </DropdownMenu.Item>
    {#if banned !== undefined}
      <DropdownMenu.Item onSelect={() => banForm?.requestSubmit()}>{banned ? 'Unban' : 'Ban'}</DropdownMenu.Item>
    {/if}
  </DropdownMenu.Content>
</DropdownMenu.Root>
