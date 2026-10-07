<script lang="ts">
  import * as Card from '#lib/components/ui/card/index.js';
  import { isAdminUser } from '#lib/types/user.js';

  let { data } = $props();

  let profile = $derived(data.profile);
  // User-supplied; only link http(s) so a `javascript:` URL can't run script.
  let safeWebsite = $derived(profile.website && /^https?:\/\//i.test(profile.website) ? profile.website : undefined);
</script>

<div class="mx-auto max-w-2xl px-4 py-12">
  <Card.Root>
    <Card.Header>
      <Card.Title>{profile.username}</Card.Title>
      <Card.Description>Joined {new Date(profile.createdAt).toLocaleDateString()}</Card.Description>
    </Card.Header>
    <Card.Content>
      <dl class="grid grid-cols-[max-content_1fr] gap-x-6 gap-y-2 text-sm">
        <dt class="text-muted-foreground">Country</dt>
        <dd>{profile.country ?? '—'}</dd>
        <dt class="text-muted-foreground">Website</dt>
        <dd>
          {#if safeWebsite}
            <a href={safeWebsite} rel="noreferrer nofollow" target="_blank" class="underline">{safeWebsite}</a>
          {:else}
            {profile.website ?? '—'}
          {/if}
        </dd>
        {#if isAdminUser(profile)}
          <dt class="text-muted-foreground">Email</dt>
          <dd>{profile.email}</dd>
          <dt class="text-muted-foreground">Role</dt>
          <dd>{profile.role}</dd>
          <dt class="text-muted-foreground">Verified</dt>
          <dd>{profile.isVerified ? 'Yes' : 'No'}</dd>
          <dt class="text-muted-foreground">Hidden</dt>
          <dd>{profile.isHidden ? 'Yes' : 'No'}</dd>
          <dt class="text-muted-foreground">Banned</dt>
          <dd>{profile.isBanned ? 'Yes' : 'No'}</dd>
          <dt class="text-muted-foreground">Two-factor</dt>
          <dd>{profile.totpEnabled ? 'Enabled' : 'Disabled'}</dd>
        {/if}
      </dl>
    </Card.Content>
  </Card.Root>
</div>
