<script lang="ts">
  import { toast } from 'svelte-sonner';
  import { superForm, type Infer, type SuperValidated } from 'sveltekit-superforms';
  import { zod4Client } from 'sveltekit-superforms/adapters';

  import * as Form from '#lib/components/ui/form/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { submitFlagSchema, type SubmitFlagResult } from '#lib/schemas/flag.js';

  let {
    data,
    attemptsLeft,
  }: {
    data: SuperValidated<Infer<typeof submitFlagSchema>, SubmitFlagResult | string>;
    /** From the page data, which reloads after each guess; null without a limit. */
    attemptsLeft: number | null;
  } = $props();

  /** The last wrong guess or api error, shown next to the input rather than in a corner toast. */
  let feedback = $state<string>();
  const outOfAttempts = $derived(attemptsLeft === 0);

  // Created once from the initial data; superForm keeps it up to date itself.
  // svelte-ignore state_referenced_locally
  const form = superForm(data, {
    validators: zod4Client(submitFlagSchema),
    onUpdate({ form: result }) {
      const outcome = result.message;
      feedback = undefined;
      // A string is an error from the api, e.g. out of attempts.
      if (typeof outcome === 'string') feedback = outcome;
      // Solved either way: the reloaded page swaps this form for the solved state, so the toast is what's left.
      else if (outcome?.alreadySolved) toast.info(m.flag_submission_already_solved());
      else if (outcome?.correct) toast.success(m.flag_submission_correct());
      // The attempts left come with the reloaded page.
      else if (outcome) feedback = m.flag_submission_wrong();
    },
    // Thrown errors would otherwise replace the page with the error page.
    onError({ result }) {
      feedback = result.error.message;
    },
  });
  const { form: formData, enhance, submitting, delayed } = form;
</script>

<form method="post" action="?/submitFlag" class="flex flex-col gap-2" use:enhance>
  <div class="flex items-start gap-2">
    <Form.Field {form} name="flag" class="flex-1">
      <Form.Control>
        {#snippet children({ props })}
          <Input
            {...props}
            bind:value={$formData.flag}
            autocomplete="off"
            spellcheck={false}
            placeholder={'flag{...}'}
            aria-label={m.flag_submission_title()}
            disabled={outOfAttempts}
          />
        {/snippet}
      </Form.Control>
      <Form.FieldErrors />
    </Form.Field>
    <Form.Button disabled={$submitting || outOfAttempts} pending={$delayed}>{m.flag_submission_submit()}</Form.Button>
  </div>
  <p role="status" class="flex flex-wrap gap-x-2 text-sm empty:hidden">
    {#if feedback}<span class="font-medium text-destructive">{feedback}</span>{/if}
    {#if outOfAttempts}
      <span class="text-destructive">{m.flag_submission_no_attempts()}</span>
    {:else if attemptsLeft !== null}
      <span class="text-muted-foreground tabular-nums">{m.flag_submission_attempts_left({ count: attemptsLeft })}</span>
    {/if}
  </p>
</form>
