<script lang="ts">
  import PlusIcon from '@lucide/svelte/icons/plus';
  import { toast } from 'svelte-sonner';
  import { superForm, type Infer, type SuperValidated } from 'sveltekit-superforms';
  import { zod4Client } from 'sveltekit-superforms/adapters';

  import { Button } from '#lib/components/ui/button/index.js';
  import { Checkbox } from '#lib/components/ui/checkbox/index.js';
  import * as Dialog from '#lib/components/ui/dialog/index.js';
  import * as Form from '#lib/components/ui/form/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import * as Select from '#lib/components/ui/select/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { createFlagSchema, decayFunctions } from '#lib/schemas/flag.js';

  import { decayLabel } from './flag-labels.js';

  let { data }: { data: SuperValidated<Infer<typeof createFlagSchema>> } = $props();

  let open = $state(false);

  // Created once from the initial data; superForm keeps it up to date itself.
  // svelte-ignore state_referenced_locally
  const form = superForm(data, {
    validators: zod4Client(createFlagSchema),
    // JSON keeps the numbers, the checkbox and the select typed; the dialog needs JavaScript anyway.
    dataType: 'json',
    onUpdate({ form: result, result: { type } }) {
      // Field errors (e.g. an invalid pattern) show inline; the rest as a toast. Stays open either way.
      if (result.message) toast.error(result.message);
      else if (type === 'success') {
        toast.success(m.challenge_flags_created());
        open = false;
      }
    },
    // Thrown errors (e.g. 403) would otherwise replace the page with the error page.
    onError({ result }) {
      toast.error(result.error.message);
    },
  });
  const { form: formData, enhance, submitting } = form;

  function onOpenChange(value: boolean) {
    if (value) form.reset();
  }
</script>

<Dialog.Root bind:open {onOpenChange}>
  <Dialog.Trigger>
    {#snippet child({ props })}
      <Button {...props} variant="outline" size="sm"><PlusIcon />{m.challenge_flags_add()}</Button>
    {/snippet}
  </Dialog.Trigger>
  <Dialog.Content>
    <Dialog.Header>
      <Dialog.Title>{m.challenge_flags_add()}</Dialog.Title>
      <Dialog.Description>{m.challenge_flags_add_description()}</Dialog.Description>
    </Dialog.Header>
    <form method="post" action="?/createFlag" class="flex flex-col gap-4" use:enhance>
      <Form.Field {form} name="pattern">
        <Form.Control>
          {#snippet children({ props })}
            <Form.Label>{m.challenge_flags_field_pattern()}</Form.Label>
            <Input
              {...props}
              bind:value={$formData.pattern}
              autocomplete="off"
              spellcheck={false}
              placeholder={'flag\\{[a-z0-9_]+\\}'}
            />
          {/snippet}
        </Form.Control>
        <Form.Description>
          {m.challenge_flags_field_pattern_hint({ example: 'flag\\{secret\\}', literal: 'flag{secret}' })}
        </Form.Description>
        <Form.FieldErrors />
      </Form.Field>
      <Form.Field {form} name="isCaseSensitive">
        <Form.Control>
          {#snippet children({ props })}
            <div class="flex items-center gap-2">
              <Checkbox {...props} bind:checked={$formData.isCaseSensitive} />
              <Form.Label>{m.challenge_flags_field_case_sensitive()}</Form.Label>
            </div>
          {/snippet}
        </Form.Control>
      </Form.Field>
      <div class="grid grid-cols-2 gap-4">
        <Form.Field {form} name="baseValue">
          <Form.Control>
            {#snippet children({ props })}
              <Form.Label>{m.challenge_flags_field_value()}</Form.Label>
              <Input {...props} type="number" min={0} step={1} bind:value={$formData.baseValue} />
            {/snippet}
          </Form.Control>
          <Form.FieldErrors />
        </Form.Field>
        <Form.Field {form} name="decayFun">
          <Form.Control>
            {#snippet children({ props })}
              <Form.Label>{m.challenge_flags_field_decay()}</Form.Label>
              <Select.Root type="single" bind:value={$formData.decayFun} allowDeselect={false}>
                <Select.Trigger {...props} class="w-full">{decayLabel($formData.decayFun)}</Select.Trigger>
                <Select.Content>
                  {#each decayFunctions as decay (decay)}
                    <Select.Item value={decay} label={decayLabel(decay)} />
                  {/each}
                </Select.Content>
              </Select.Root>
            {/snippet}
          </Form.Control>
          <Form.FieldErrors />
        </Form.Field>
        {#if $formData.decayFun !== 'static'}
          <Form.Field {form} name="decayValue">
            <Form.Control>
              {#snippet children({ props })}
                <Form.Label>{m.challenge_flags_field_decay_value()}</Form.Label>
                <Input {...props} type="number" min={0} step={1} bind:value={$formData.decayValue} />
              {/snippet}
            </Form.Control>
            <Form.FieldErrors />
          </Form.Field>
          <Form.Field {form} name="minValue">
            <Form.Control>
              {#snippet children({ props })}
                <Form.Label>{m.challenge_flags_field_min_value()}</Form.Label>
                <Input {...props} type="number" min={0} step={1} bind:value={$formData.minValue} />
              {/snippet}
            </Form.Control>
            <Form.FieldErrors />
          </Form.Field>
        {/if}
      </div>
      <Dialog.Footer>
        <Dialog.Close>
          {#snippet child({ props })}
            <Button variant="outline" {...props}>{m.challenges_create_cancel()}</Button>
          {/snippet}
        </Dialog.Close>
        <Form.Button disabled={$submitting}>{m.challenges_create_submit()}</Form.Button>
      </Dialog.Footer>
    </form>
  </Dialog.Content>
</Dialog.Root>
