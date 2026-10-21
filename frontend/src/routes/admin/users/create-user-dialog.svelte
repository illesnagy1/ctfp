<script lang="ts">
  import { enhance } from '$app/forms';
  import UserPlusIcon from '@lucide/svelte/icons/user-plus';
  import { toast } from 'svelte-sonner';

  import { Button } from '#lib/components/ui/button/index.js';
  import { Checkbox } from '#lib/components/ui/checkbox/index.js';
  import * as Dialog from '#lib/components/ui/dialog/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import { Label } from '#lib/components/ui/label/index.js';
  import * as Select from '#lib/components/ui/select/index.js';
  import { roleLabel, roleOptions } from '#lib/components/users/labels.js';
  import { actionError } from '#lib/form-action.js';
  import { m } from '#lib/paraglide/messages.js';
  import type { AdminUser } from '#lib/types/user.js';

  let open = $state(false);
  let pending = $state(false);
  let role = $state<AdminUser['role']>('USER');

  function onOpenChange(value: boolean) {
    if (!value) return;
    role = 'USER';
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
    <form
      method="post"
      action="?/create"
      class="flex flex-col gap-4"
      use:enhance={({ formData }) => {
        pending = true;
        const username = String(formData.get('username')).trim();
        return async ({ result, update }) => {
          pending = false;
          const error = actionError(result);
          if (error) {
            // Stays open with what was typed, so it can be fixed.
            toast.error(error);
            return;
          }
          await update();
          if (result.type === 'success') {
            toast.success(m.users_created({ username }));
            open = false;
          }
        };
      }}
    >
      <!-- Limits mirror the api's validation (UserValidation.kt), which has the final say. -->
      <div class="flex flex-col gap-2">
        <Label for="user-username">{m.users_column_username()}</Label>
        <Input
          id="user-username"
          name="username"
          autocomplete="off"
          minlength={3}
          maxlength={64}
          pattern="[A-Za-z0-9._\-]+"
          required
        />
      </div>
      <div class="flex flex-col gap-2">
        <Label for="user-email">{m.users_column_email()}</Label>
        <Input id="user-email" name="email" type="email" autocomplete="off" maxlength={255} required />
      </div>
      <div class="flex flex-col gap-2">
        <Label for="user-password">{m.users_field_password()}</Label>
        <Input
          id="user-password"
          name="password"
          type="password"
          autocomplete="new-password"
          minlength={8}
          maxlength={128}
          required
        />
      </div>
      <div class="flex flex-col gap-2">
        <Label for="user-role">{m.users_column_role()}</Label>
        <Select.Root type="single" name="role" bind:value={role} allowDeselect={false}>
          <Select.Trigger id="user-role" class="w-full">{roleLabel(role)}</Select.Trigger>
          <Select.Content>
            {#each roleOptions() as option (option.value)}
              <Select.Item value={option.value} label={option.label} />
            {/each}
          </Select.Content>
        </Select.Root>
      </div>
      <div class="flex flex-col gap-3">
        <div class="flex items-center gap-2">
          <Checkbox id="user-verified" name="isVerified" checked />
          <Label for="user-verified">{m.user_profile_verified()}</Label>
        </div>
        <div class="flex items-center gap-2">
          <Checkbox id="user-hidden" name="isHidden" />
          <Label for="user-hidden">{m.users_status_hidden()}</Label>
        </div>
      </div>
      <Dialog.Footer>
        <Dialog.Close>
          {#snippet child({ props })}
            <Button variant="outline" {...props}>{m.users_create_cancel()}</Button>
          {/snippet}
        </Dialog.Close>
        <Button type="submit" disabled={pending}>{m.users_create_submit()}</Button>
      </Dialog.Footer>
    </form>
  </Dialog.Content>
</Dialog.Root>
