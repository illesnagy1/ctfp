<script lang="ts">
  import LocalTime from '#lib/components/local-time.svelte';
  import * as Card from '#lib/components/ui/card/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { type User, isAdminUser } from '#lib/types/user.js';

  import { roleLabel } from './labels.js';

  /** Shows the account details too when given the full view (own account, or an admin looking). */
  let { user }: { user: User } = $props();

  // User-supplied; only link http(s) so a `javascript:` URL can't run script.
  let safeWebsite = $derived(user.website && /^https?:\/\//i.test(user.website) ? user.website : undefined);

  const yesNo = (value: boolean | undefined) => (value ? m.user_profile_yes() : m.user_profile_no());
</script>

<Card.Root>
  <Card.Header>
    <Card.Title>{user.username}</Card.Title>
    <Card.Description>
      <LocalTime value={user.createdAt} dateOnly>
        {#snippet children(date)}{m.user_profile_joined({ date })}{/snippet}
      </LocalTime>
    </Card.Description>
  </Card.Header>
  <Card.Content>
    <dl class="grid grid-cols-label-value gap-x-6 gap-y-2 text-sm">
      <dt class="text-muted-foreground">{m.users_column_country()}</dt>
      <dd>{user.country ?? '-'}</dd>
      <dt class="text-muted-foreground">{m.users_column_website()}</dt>
      <dd>
        {#if safeWebsite}
          <a href={safeWebsite} rel="noreferrer nofollow" target="_blank" class="underline">{safeWebsite}</a>
        {:else}
          {user.website ?? '-'}
        {/if}
      </dd>
      {#if isAdminUser(user)}
        <dt class="text-muted-foreground">{m.users_column_email()}</dt>
        <dd>{user.email}</dd>
        <dt class="text-muted-foreground">{m.users_column_role()}</dt>
        <dd>{roleLabel(user.role)}</dd>
        <dt class="text-muted-foreground">{m.user_profile_verified()}</dt>
        <dd>{yesNo(user.isVerified)}</dd>
        <dt class="text-muted-foreground">{m.users_status_hidden()}</dt>
        <dd>{yesNo(user.isHidden)}</dd>
        <dt class="text-muted-foreground">{m.users_status_banned()}</dt>
        <dd>{yesNo(user.isBanned)}</dd>
        <dt class="text-muted-foreground">{m.user_profile_two_factor()}</dt>
        <dd>{user.totpEnabled ? m.user_profile_enabled() : m.user_profile_disabled()}</dd>
      {/if}
    </dl>
  </Card.Content>
</Card.Root>
