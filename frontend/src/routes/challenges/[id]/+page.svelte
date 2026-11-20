<script lang="ts">
  import { enhance } from '$app/forms';
  import { resolve } from '$app/paths';
  import ArrowLeftIcon from '@lucide/svelte/icons/arrow-left';
  import CircleCheckIcon from '@lucide/svelte/icons/circle-check';
  import FlagIcon from '@lucide/svelte/icons/flag';
  import TimerIcon from '@lucide/svelte/icons/timer';
  import { toast } from 'svelte-sonner';

  import { DataTableBadgesCell } from '#lib/components/data-table/index.js';
  import LocalTime from '#lib/components/local-time.svelte';
  import { Badge } from '#lib/components/ui/badge/index.js';
  import { Button } from '#lib/components/ui/button/index.js';
  import * as Card from '#lib/components/ui/card/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { getLocale } from '#lib/paraglide/runtime.js';
  import { duration } from '#lib/time.js';

  import ChallengeFiles from './challenge-files.svelte';
  import ChallengeFlags from './challenge-flags.svelte';
  import FlagSubmission from './flag-submission.svelte';

  let { data } = $props();

  const challenge = $derived(data.challenge);
  const isAdmin = $derived(data.user?.role === 'ADMIN');

  // The api hides the description and files of a timed challenge until a player starts it.
  const timed = $derived(challenge.timeLimit > 0 && !isAdmin);
  const notStarted = $derived(timed && !challenge.startedAt);
  const deadline = $derived(challenge.startedAt ? Date.parse(challenge.startedAt) + challenge.timeLimit * 1000 : 0);

  let now = $state(Date.now());
  const remaining = $derived(Math.max(0, Math.ceil((deadline - now) / 1000)));
  const timeUp = $derived(timed && !!challenge.startedAt && remaining === 0);

  $effect(() => {
    if (!timed || !challenge.startedAt) return;
    now = Date.now();
    const interval = setInterval(() => {
      now = Date.now();
      if (now >= deadline) clearInterval(interval);
    }, 1000);
    return () => clearInterval(interval);
  });

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
        <DataTableBadgesCell badges={challenge.categories} />
      {/if}
      <Card.Action>
        <Badge>{m.challenge_points({ value: challenge.value ?? 0 })}</Badge>
      </Card.Action>
    </Card.Header>
    <Card.Content>
      <div class="flex flex-col gap-6">
        {#if notStarted}
          <section class="flex flex-col gap-3 rounded-md border p-4" aria-labelledby="timed-title">
            <h2 id="timed-title" class="flex items-center gap-2 text-sm font-medium">
              <TimerIcon class="size-4" />
              {m.challenge_timed_title()}
            </h2>
            <p class="text-sm text-muted-foreground">
              {m.challenge_timed_intro({ duration: duration(challenge.timeLimit, getLocale()) })}
            </p>
            {#if data.user}
              <form
                method="post"
                action="?/start"
                class="self-start"
                use:enhance={() =>
                  async ({ result, update }) => {
                    if (result.type === 'failure')
                      toast.error(String(result.data?.message ?? m.challenge_timed_start_failed()));
                    else if (result.type === 'error') toast.error(result.error.message);
                    await update();
                  }}
              >
                <Button type="submit">{m.challenge_timed_start()}</Button>
              </form>
            {:else}
              <a href={resolve('/(auth)/login')} class="text-sm underline">{m.challenge_timed_login()}</a>
            {/if}
          </section>
        {:else}
          {#if timed && challenge.startedAt}
            <p
              class="flex items-center gap-2 rounded-md border p-3 text-sm font-medium tabular-nums {timeUp
                ? 'text-destructive'
                : ''}"
              role="timer"
            >
              <TimerIcon class="size-4" />
              {timeUp
                ? m.challenge_timed_up()
                : m.challenge_timed_remaining({ duration: duration(remaining, getLocale()) })}
            </p>
          {/if}
          {#if data.descriptionHtml}
            <div class="prose prose-sm max-w-none dark:prose-invert">
              <!-- eslint-disable-next-line svelte/no-at-html-tags -- sanitized by renderMarkdown in +page.server.ts -->
              {@html data.descriptionHtml}
            </div>
          {:else}
            <p class="text-sm text-muted-foreground">{m.challenge_details_no_description()}</p>
          {/if}
          <section class="flex flex-col gap-3" aria-labelledby="flag-submission-title">
            <h2 id="flag-submission-title" class="flex items-center gap-2 text-sm font-medium">
              <FlagIcon class="size-4" />
              {m.flag_submission_title()}
            </h2>
            {#if challenge.solved}
              <p class="flex items-center gap-2 rounded-md border bg-success p-3 text-sm font-medium">
                <CircleCheckIcon class="size-4" />
                {m.challenge_solved()}
              </p>
            {:else if data.submitFlagForm}
              <FlagSubmission data={data.submitFlagForm} attemptsLeft={challenge.attemptsLeft ?? null} {timeUp} />
            {:else}
              <a href={resolve('/(auth)/login')} class="text-sm underline">{m.flag_submission_login()}</a>
            {/if}
          </section>
        {/if}
        <dl class="grid grid-cols-label-value gap-x-6 gap-y-2 text-sm">
          <dt class="text-muted-foreground">{m.challenges_column_max_attempts()}</dt>
          <dd>{challenge.maxAttempts > 0 ? challenge.maxAttempts : m.challenge_details_no_limit()}</dd>
          <dt class="text-muted-foreground">{m.challenge_details_time_limit()}</dt>
          <dd>
            {challenge.timeLimit > 0 ? duration(challenge.timeLimit, getLocale()) : m.challenge_details_no_limit()}
          </dd>
          {#if isAdmin}
            <!-- Players only ever see published challenges. -->
            <dt class="text-muted-foreground">{m.challenges_field_publish_at()}</dt>
            <dd><LocalTime value={challenge.publishAt} /></dd>
            <dt class="text-muted-foreground">{m.challenges_field_visible()}</dt>
            <dd>{yesNo(challenge.isVisible)}</dd>
            <dt class="text-muted-foreground">{m.challenge_details_created()}</dt>
            <dd><LocalTime value={challenge.createdAt} /></dd>
            <dt class="text-muted-foreground">{m.challenge_details_updated()}</dt>
            <dd><LocalTime value={challenge.updatedAt} /></dd>
          {/if}
        </dl>
        {#if !notStarted}<ChallengeFiles challengeId={challenge.id} files={data.files} {isAdmin} />{/if}
        {#if data.flags && data.createFlagForm}
          <ChallengeFlags flags={data.flags} createForm={data.createFlagForm} />
        {/if}
      </div>
    </Card.Content>
  </Card.Root>
</div>
