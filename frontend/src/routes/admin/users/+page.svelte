<script lang="ts">
  import BanIcon from '@lucide/svelte/icons/ban';
  import UndoIcon from '@lucide/svelte/icons/undo-2';
  import { toast } from 'svelte-sonner';

  import { DataTable } from '#lib/components/data-table/index.js';
  import { Button } from '#lib/components/ui/button/index.js';
  import { roleOptions } from '#lib/components/users/labels.js';
  import { submitAction } from '#lib/form-action.js';
  import { m } from '#lib/paraglide/messages.js';
  import type { AdminUser } from '#lib/types/user.js';

  import { columns } from './columns.js';
  import CreateUserDialog from './create-user-dialog.svelte';

  let { data } = $props();

  let pending = $state(false);

  async function setBanned(users: AdminUser[], banned: boolean) {
    pending = true;
    try {
      await submitAction('?/setBannedMany', { ids: users.map((u) => u.id).join(','), banned: String(banned) });
      const count = users.length;
      toast.success(banned ? m.users_banned_many({ count }) : m.users_unbanned_many({ count }));
    } catch (error) {
      toast.error(error instanceof Error ? error.message : m.form_action_failed());
    } finally {
      pending = false;
    }
  }
</script>

{#snippet bulkActions(selected: AdminUser[])}
  {@const toBan = selected.filter((u) => !u.isBanned)}
  {@const toUnban = selected.filter((u) => u.isBanned)}
  <!-- Your own account is never banned, see setBannedMany. -->
  {@const includesSelf = toBan.some((u) => u.id === data.user?.id)}
  {#if includesSelf}
    <span class="text-sm text-muted-foreground">{m.users_ban_self()}</span>
  {/if}
  {#if toBan.length > 0}
    <Button variant="destructive" size="sm" disabled={pending || includesSelf} onclick={() => setBanned(toBan, true)}>
      <BanIcon />
      {m.users_ban_selected({ count: toBan.length })}
    </Button>
  {/if}
  {#if toUnban.length > 0}
    <Button variant="outline" size="sm" disabled={pending} onclick={() => setBanned(toUnban, false)}>
      <UndoIcon />
      {m.users_unban_selected({ count: toUnban.length })}
    </Button>
  {/if}
{/snippet}

<div class="mx-auto w-full max-w-6xl px-4 py-6">
  <div class="flex items-center justify-between gap-4 pb-4">
    <h1 class="text-2xl font-semibold tracking-tight">{m.users_title()}</h1>
    <CreateUserDialog />
  </div>
  <DataTable
    data={data.users}
    {columns}
    getRowId={(user) => String(user.id)}
    actions={bulkActions}
    search={{ column: 'username', placeholder: m.users_filter_placeholder() }}
    filters={[{ column: 'role', title: m.users_column_role(), options: roleOptions() }]}
  />
</div>
