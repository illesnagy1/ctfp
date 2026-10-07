<script lang="ts">
  import * as Card from '#lib/components/ui/card/index.js';
  import { type User, isAdminUser } from '#lib/types/user.js';

  /** Shows the account details too when given the full view (own account, or an admin looking). */
  let { user }: { user: User } = $props();

  // User-supplied; only link http(s) so a `javascript:` URL can't run script.
  let safeWebsite = $derived(user.website && /^https?:\/\//i.test(user.website) ? user.website : undefined);
</script>

<Card.Root>
  <Card.Header>
    <Card.Title>{user.username}</Card.Title>
    <Card.Description>Joined {new Date(user.createdAt).toLocaleDateString()}</Card.Description>
  </Card.Header>
  <Card.Content>
    <dl class="grid grid-cols-[max-content_1fr] gap-x-6 gap-y-2 text-sm">
      <dt class="text-muted-foreground">Country</dt>
      <dd>{user.country ?? '-'}</dd>
      <dt class="text-muted-foreground">Website</dt>
      <dd>
        {#if safeWebsite}
          <a href={safeWebsite} rel="noreferrer nofollow" target="_blank" class="underline">{safeWebsite}</a>
        {:else}
          {user.website ?? '-'}
        {/if}
      </dd>
      {#if isAdminUser(user)}
        <dt class="text-muted-foreground">Email</dt>
        <dd>{user.email}</dd>
        <dt class="text-muted-foreground">Role</dt>
        <dd>{user.role}</dd>
        <dt class="text-muted-foreground">Verified</dt>
        <dd>{user.isVerified ? 'Yes' : 'No'}</dd>
        <dt class="text-muted-foreground">Hidden</dt>
        <dd>{user.isHidden ? 'Yes' : 'No'}</dd>
        <dt class="text-muted-foreground">Banned</dt>
        <dd>{user.isBanned ? 'Yes' : 'No'}</dd>
        <dt class="text-muted-foreground">Two-factor</dt>
        <dd>{user.totpEnabled ? 'Enabled' : 'Disabled'}</dd>
      {/if}
    </dl>
  </Card.Content>
</Card.Root>
