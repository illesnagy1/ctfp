<script lang="ts">
  import PencilIcon from '@lucide/svelte/icons/pencil';
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
  import { editUserSchema } from '#lib/schemas/user.js';

  let {
    data,
    admin,
    self,
    action = '?/update',
    open = $bindable(false),
    trigger = true,
  }: {
    /** The saved profile; give each user's form its own id, e.g. `edit-user-5`, so several can be on a page. */
    data: SuperValidated<Infer<typeof editUserSchema>>;
    /** Shows the account fields: role, verified, hidden, banned. */
    admin: boolean;
    /** Editing your own account; an admin can't change their own role or ban themselves. */
    self: boolean;
    /** The user page's `update` action; from another page, e.g. `/users/5?/update`. */
    action?: string;
    open?: boolean;
    /** Shows the "Edit profile" button; without it, mount the dialog with `open` set, fresh each time. */
    trigger?: boolean;
  } = $props();

  // Created once from the initial data; superForm keeps it up to date itself.
  // svelte-ignore state_referenced_locally
  const form = superForm(data, {
    validators: zod4Client(editUserSchema),
    // JSON keeps the booleans and the role select typed; the dialog needs JavaScript anyway.
    dataType: 'json',
    resetForm: false,
    onUpdate({ form: result, result: { type } }) {
      // Field errors show inline; the rest (e.g. a taken username) as a toast. Stays open either way.
      if (result.message) toast.error(result.message);
      else if (type === 'success') {
        toast.success(m.user_updated());
        open = false;
      }
    },
    // Thrown errors (e.g. 403) would otherwise replace the page with the error page.
    onError({ result }) {
      toast.error(result.error.message);
    },
  });
  const { form: formData, enhance, submitting } = form;

  // Each time the button opens it, start from the saved profile, which the page reloads after each save.
  function onOpenChange(value: boolean) {
    if (value) form.reset({ newState: data.data });
  }
</script>

<Dialog.Root bind:open {onOpenChange}>
  {#if trigger}
    <Dialog.Trigger>
      {#snippet child({ props })}
        <Button variant="outline" size="sm" {...props}><PencilIcon />{m.user_edit()}</Button>
      {/snippet}
    </Dialog.Trigger>
  {/if}
  <Dialog.Content>
    <Dialog.Header>
      <Dialog.Title>{m.user_edit()}</Dialog.Title>
      <Dialog.Description>{m.user_edit_description()}</Dialog.Description>
    </Dialog.Header>
    <form method="post" {action} class="flex flex-col gap-4" use:enhance>
      <Form.Field {form} name="username">
        <Form.Control>
          {#snippet children({ props })}
            <Form.Label>{m.users_column_username()}</Form.Label>
            <Input {...props} autocomplete="username" bind:value={$formData.username} />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>
      <Form.Field {form} name="email">
        <Form.Control>
          {#snippet children({ props })}
            <Form.Label>{m.users_column_email()}</Form.Label>
            <Input {...props} type="email" autocomplete="email" bind:value={$formData.email} />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>
      <Form.Field {form} name="country">
        <Form.Control>
          {#snippet children({ props })}
            <Form.Label>{m.users_column_country()}</Form.Label>
            <Input {...props} maxlength={2} autocomplete="country" bind:value={$formData.country} />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>
      <Form.Field {form} name="website">
        <Form.Control>
          {#snippet children({ props })}
            <Form.Label>{m.users_column_website()}</Form.Label>
            <Input {...props} type="url" autocomplete="url" placeholder="https://" bind:value={$formData.website} />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>
      {#if admin}
        <Form.Field {form} name="role">
          <Form.Control>
            {#snippet children({ props })}
              <Form.Label>{m.users_column_role()}</Form.Label>
              <Select.Root type="single" bind:value={$formData.role} allowDeselect={false} disabled={self}>
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
          <Form.Field {form} name="isBanned">
            <Form.Control>
              {#snippet children({ props })}
                <div class="flex items-center gap-2">
                  <Checkbox {...props} bind:checked={$formData.isBanned} disabled={self} />
                  <Form.Label>{m.users_status_banned()}</Form.Label>
                </div>
              {/snippet}
            </Form.Control>
          </Form.Field>
        </div>
        {#if self}
          <p class="text-sm text-muted-foreground">{m.user_edit_self_locked()}</p>
        {/if}
      {/if}
      <Dialog.Footer>
        <Dialog.Close>
          {#snippet child({ props })}
            <Button variant="outline" {...props}>{m.users_create_cancel()}</Button>
          {/snippet}
        </Dialog.Close>
        <Form.Button disabled={$submitting}>{m.user_edit_submit()}</Form.Button>
      </Dialog.Footer>
    </form>
  </Dialog.Content>
</Dialog.Root>
