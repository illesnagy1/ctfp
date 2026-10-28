<script lang="ts">
  import { toast } from 'svelte-sonner';
  import { superForm, type Infer, type SuperValidated } from 'sveltekit-superforms';
  import { zod4Client } from 'sveltekit-superforms/adapters';

  import * as Form from '#lib/components/ui/form/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { submitFlagSchema, type SubmitFlagResult } from '#lib/schemas/flag.js';

  let { data }: { data: SuperValidated<Infer<typeof submitFlagSchema>, SubmitFlagResult | string> } = $props();

  // Created once from the initial data; superForm keeps it up to date itself.
  // svelte-ignore state_referenced_locally
  const form = superForm(data, {
    validators: zod4Client(submitFlagSchema),
    onUpdate({ form: result }) {
      const outcome = result.message;
      // A string is an error from the api, e.g. out of attempts.
      if (typeof outcome === 'string') toast.error(outcome);
      else if (outcome?.alreadySolved) toast.info(m.flag_submission_already_solved());
      else if (outcome?.correct) toast.success(m.flag_submission_correct());
      else if (outcome) {
        const attempts = outcome.attemptsLeft;
        toast.error(m.flag_submission_wrong(), {
          description: attempts === null ? undefined : m.flag_submission_attempts_left({ count: attempts }),
        });
      }
    },
    // Thrown errors would otherwise replace the page with the error page.
    onError({ result }) {
      toast.error(result.error.message);
    },
  });
  const { form: formData, enhance, submitting } = form;
</script>

<form method="post" action="?/submitFlag" class="flex items-start gap-2" use:enhance>
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
        />
      {/snippet}
    </Form.Control>
    <Form.FieldErrors />
  </Form.Field>
  <Form.Button disabled={$submitting}>{m.flag_submission_submit()}</Form.Button>
</form>
