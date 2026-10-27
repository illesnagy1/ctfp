<script lang="ts">
  import PlusIcon from '@lucide/svelte/icons/plus';
  import { toast } from 'svelte-sonner';
  import { superForm, type Infer, type SuperValidated } from 'sveltekit-superforms';
  import { zod4Client } from 'sveltekit-superforms/adapters';

  import DateTimePicker from '#lib/components/date-time-picker.svelte';
  import { Button } from '#lib/components/ui/button/index.js';
  import { Checkbox } from '#lib/components/ui/checkbox/index.js';
  import * as Dialog from '#lib/components/ui/dialog/index.js';
  import * as Form from '#lib/components/ui/form/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import { TagsInput } from '#lib/components/ui/tags-input/index.js';
  import { Textarea } from '#lib/components/ui/textarea/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { createChallengeSchema } from '#lib/schemas/challenge.js';

  let {
    data,
    knownCategories = [],
  }: {
    data: SuperValidated<Infer<typeof createChallengeSchema>>;
    /** Categories already in use, offered as suggestions. */
    knownCategories?: string[];
  } = $props();

  let open = $state(false);

  // Created once from the initial data; superForm keeps it up to date itself.
  // svelte-ignore state_referenced_locally
  const form = superForm(data, {
    validators: zod4Client(createChallengeSchema),
    // JSON keeps the categories array and the numbers typed; the dialog needs JavaScript anyway.
    dataType: 'json',
    onUpdate({ form: result, result: { type } }) {
      // Field errors (e.g. a taken title) show inline; the rest as a toast. Stays open either way.
      if (result.message) toast.error(result.message);
      else if (type === 'success') {
        toast.success(m.challenges_created({ title: result.data.title }));
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
    if (value) form.reset({ data: { publishAt: new Date().toISOString() } });
  }
</script>

<Dialog.Root bind:open {onOpenChange}>
  <Dialog.Trigger>
    {#snippet child({ props })}
      <Button {...props}><PlusIcon />{m.challenges_create()}</Button>
    {/snippet}
  </Dialog.Trigger>
  <Dialog.Content class="sm:max-w-lg">
    <Dialog.Header>
      <Dialog.Title>{m.challenges_create()}</Dialog.Title>
      <Dialog.Description>{m.challenges_create_description()}</Dialog.Description>
    </Dialog.Header>
    <form method="post" action="?/create" class="flex flex-col gap-4" use:enhance>
      <Form.Field {form} name="title">
        <Form.Control>
          {#snippet children({ props })}
            <Form.Label>{m.challenges_field_title()}</Form.Label>
            <Input {...props} bind:value={$formData.title} />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>
      <Form.Field {form} name="description">
        <Form.Control>
          {#snippet children({ props })}
            <Form.Label>{m.challenges_field_description()}</Form.Label>
            <Textarea {...props} rows={4} bind:value={$formData.description} />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>
      <Form.Field {form} name="categories">
        <Form.Control>
          {#snippet children({ props })}
            <Form.Label>{m.challenges_field_categories()}</Form.Label>
            <TagsInput
              {...props}
              bind:value={$formData.categories}
              suggestions={knownCategories}
              placeholder={m.challenges_field_categories_placeholder()}
            />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>
      <Form.Field {form} name="publishAt">
        <Form.Control>
          {#snippet children({ props })}
            <Form.Label>{m.challenges_field_publish_at()}</Form.Label>
            <DateTimePicker bind:value={$formData.publishAt} triggerProps={props} />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>
      <div class="grid grid-cols-2 gap-4">
        <Form.Field {form} name="maxAttempts">
          <Form.Control>
            {#snippet children({ props })}
              <Form.Label>{m.challenges_field_max_attempts()}</Form.Label>
              <Input {...props} type="number" min={0} step={1} bind:value={$formData.maxAttempts} />
            {/snippet}
          </Form.Control>
          <Form.FieldErrors />
        </Form.Field>
        <Form.Field {form} name="timeLimit">
          <Form.Control>
            {#snippet children({ props })}
              <Form.Label>{m.challenges_field_time_limit()}</Form.Label>
              <Input {...props} type="number" min={0} step={1} bind:value={$formData.timeLimit} />
            {/snippet}
          </Form.Control>
          <Form.FieldErrors />
        </Form.Field>
        <p class="col-span-2 -mt-2 text-xs text-muted-foreground">{m.challenges_field_limit_hint()}</p>
      </div>
      <Form.Field {form} name="isVisible">
        <Form.Control>
          {#snippet children({ props })}
            <div class="flex items-center gap-2">
              <Checkbox {...props} bind:checked={$formData.isVisible} />
              <Form.Label>{m.challenges_field_visible()}</Form.Label>
            </div>
          {/snippet}
        </Form.Control>
      </Form.Field>
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
