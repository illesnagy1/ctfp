<script lang="ts">
  import { resolve } from '$app/paths';
  import ArrowLeftIcon from '@lucide/svelte/icons/arrow-left';

  import LocalTime from '#lib/components/local-time.svelte';
  import * as Card from '#lib/components/ui/card/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { getLocale } from '#lib/paraglide/runtime.js';

  import CategoriesCell from '../categories-cell.svelte';
  import ChallengeFiles from './challenge-files.svelte';

  let { data } = $props();

  const challenge = $derived(data.challenge);
  const isAdmin = $derived(data.user?.role === 'ADMIN');

  /** Seconds as e.g. "1h 30m", in the current locale's unit symbols. */
  function duration(seconds: number) {
    const parts: [number, string][] = [
      [Math.floor(seconds / 3600), 'hour'],
      [Math.floor((seconds % 3600) / 60), 'minute'],
      [seconds % 60, 'second'],
    ];
    return parts
      .filter(([amount]) => amount > 0)
      .map(([amount, unit]) =>
        new Intl.NumberFormat(getLocale(), { style: 'unit', unit, unitDisplay: 'narrow' }).format(amount),
      )
      .join(' ');
  }

  const yesNo = (value: boolean | undefined) => (value ? m.user_profile_yes() : m.user_profile_no());
</script>

<div class="mx-auto flex max-w-2xl flex-col gap-4 px-4 py-12">
  <a
    href={resolve('/challenges')}
    class="flex items-center gap-1 self-start text-sm text-muted-foreground hover:text-foreground"
  >
    <ArrowLeftIcon class="size-4" />
    {m.challenge_details_back()}
  </a>
  <Card.Root>
    <Card.Header>
      <Card.Title>{challenge.title}</Card.Title>
      {#if challenge.categories.length > 0}
        <CategoriesCell categories={challenge.categories} />
      {/if}
    </Card.Header>
    <Card.Content>
      <div class="flex flex-col gap-6">
        {#if data.descriptionHtml}
          <div class="prose prose-sm max-w-none dark:prose-invert">
            <!-- eslint-disable-next-line svelte/no-at-html-tags -- sanitized by renderMarkdown in +page.server.ts -->
            {@html data.descriptionHtml}
          </div>
        {:else}
          <p class="text-sm text-muted-foreground">{m.challenge_details_no_description()}</p>
        {/if}
        <dl class="grid grid-cols-label-value gap-x-6 gap-y-2 text-sm">
          <dt class="text-muted-foreground">{m.challenges_field_publish_at()}</dt>
          <dd><LocalTime value={challenge.publishAt} /></dd>
          <dt class="text-muted-foreground">{m.challenges_column_max_attempts()}</dt>
          <dd>{challenge.maxAttempts > 0 ? challenge.maxAttempts : m.challenge_details_no_limit()}</dd>
          <dt class="text-muted-foreground">{m.challenge_details_time_limit()}</dt>
          <dd>{challenge.timeLimit > 0 ? duration(challenge.timeLimit) : m.challenge_details_no_limit()}</dd>
          {#if isAdmin}
            <dt class="text-muted-foreground">{m.challenges_field_visible()}</dt>
            <dd>{yesNo(challenge.isVisible)}</dd>
            <dt class="text-muted-foreground">{m.challenge_details_created()}</dt>
            <dd><LocalTime value={challenge.createdAt} /></dd>
            <dt class="text-muted-foreground">{m.challenge_details_updated()}</dt>
            <dd><LocalTime value={challenge.updatedAt} /></dd>
          {/if}
        </dl>
        <ChallengeFiles challengeId={challenge.id} files={data.files} {isAdmin} />
      </div>
    </Card.Content>
  </Card.Root>
</div>
