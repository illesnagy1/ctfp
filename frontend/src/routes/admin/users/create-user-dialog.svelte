<script lang="ts">
  import UserPlusIcon from '@lucide/svelte/icons/user-plus';
  import { toast } from 'svelte-sonner';
  import { superForm, type Infer, type SuperValidated } from 'sveltekit-superforms';
  import { zod4Client } from 'sveltekit-superforms/adapters';

  import { Button } from '#lib/components/ui/button/index.js';
  import { Checkbox } from '#lib/components/ui/checkbox/index.js';
  import * as Dialog from '#lib/components/ui/dialog/index.js';
  import * as Form from '#lib/components/ui/form/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import * as Select from '#lib/components/ui/select/index.js';
  import { roleLabel, roleOptions } from '#lib/components/users/labels.js';
  import { m } from '#lib/paraglide/messages.js';
  import { createUserSchema } from '#lib/schemas/user.js';

  let { data }: { data: SuperValidated<Infer<typeof createUserSchema>> } = $props();

  let open = $state(false);

  // Created once from the initial data; superForm keeps it up to date itself.
  // svelte-ignore state_referenced_locally
  const form = superForm(data, {
    validators: zod4Client(createUserSchema),
    // JSON keeps the booleans and the role select typed; the dialog needs JavaScript anyway.
    dataType: 'json',
    onUpdate({ form: result, result: { type } }) {
      // Field errors show inline; the rest (e.g. a taken username) as a toast. Stays open either way.
      if (result.message) toast.error(result.message);
      else if (type === 'success') {
        toast.success(m.users_created({ username: result.data.username }));
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
      <Button {...props}><UserPlusIcon />{m.users_create()}</Button>
    {/snippet}
  </Dialog.Trigger>
  <Dialog.Content>
    <Dialog.Header>
      <Dialog.Title>{m.users_create()}</Dialog.Title>
      <Dialog.Description>{m.users_create_description()}</Dialog.Description>
    </Dialog.Header>
    <form method="post" action="?/create" class="flex flex-col gap-4" use:enhance>
      <Form.Field {form} name="username">
        <Form.Control>
          {#snippet children({ props })}
            <Form.Label>{m.users_column_username()}</Form.Label>
            <Input {...props} autocomplete="off" bind:value={$formData.username} />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>
      <Form.Field {form} name="email">
        <Form.Control>
          {#snippet children({ props })}
            <Form.Label>{m.users_column_email()}</Form.Label>
            <Input {...props} type="email" autocomplete="off" bind:value={$formData.email} />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>
      <Form.Field {form} name="password">
        <Form.Control>
          {#snippet children({ props })}
            <Form.Label>{m.users_field_password()}</Form.Label>
            <Input {...props} type="password" autocomplete="new-password" bind:value={$formData.password} />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>
      <Form.Field {form} name="role">
        <Form.Control>
          {#snippet children({ props })}
            <Form.Label>{m.users_column_role()}</Form.Label>
            <Select.Root type="single" bind:value={$formData.role} allowDeselect={false}>
              <Select.Trigger {...props} class="w-full">{roleLabel($formData.role)}</Select.Trigger>
              <Select.Content>
                {#each roleOptions() as option (option.value)}
                  <Select.Item value={option.value} label={option.label} />
                {/each}
              </Select.Content>
            </Select.Root>
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>
      <div class="flex flex-col gap-3">
        <Form.Field {form} name="isVerified">
          <Form.Control>
            {#snippet children({ props })}
              <div class="flex items-center gap-2">
                <Checkbox {...props} bind:checked={$formData.isVerified} />
                <Form.Label>{m.user_profile_verified()}</Form.Label>
              </div>
            {/snippet}
          </Form.Control>
        </Form.Field>
        <Form.Field {form} name="isHidden">
          <Form.Control>
            {#snippet children({ props })}
              <div class="flex items-center gap-2">
                <Checkbox {...props} bind:checked={$formData.isHidden} />
                <Form.Label>{m.users_status_hidden()}</Form.Label>
              </div>
            {/snippet}
          </Form.Control>
        </Form.Field>
      </div>
      <Dialog.Footer>
        <Dialog.Close>
          {#snippet child({ props })}
            <Button variant="outline" {...props}>{m.users_create_cancel()}</Button>
          {/snippet}
        </Dialog.Close>
        <Form.Button disabled={$submitting}>{m.users_create_submit()}</Form.Button>
      </Dialog.Footer>
    </form>
  </Dialog.Content>
</Dialog.Root>
