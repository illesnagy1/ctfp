<script lang="ts">
  import { resolve } from '$app/paths';
  import { superForm } from 'sveltekit-superforms';
  import { zod4Client } from 'sveltekit-superforms/adapters';

  import * as Card from '#lib/components/ui/card/index.js';
  import * as Form from '#lib/components/ui/form/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { registerSchema } from '#lib/schemas/auth.js';

  let { data } = $props();

  // Created once from the initial data; superForm keeps it up to date itself.
  // svelte-ignore state_referenced_locally
  const form = superForm(data.form, { validators: zod4Client(registerSchema) });
  const { form: formData, enhance, message, submitting, delayed } = form;
</script>

<div class="mx-auto max-w-sm px-4 py-12">
  <Card.Root>
    <Card.Header>
      <Card.Title>{m.nav_register()}</Card.Title>
    </Card.Header>
    <Card.Content>
      <form method="post" use:enhance class="flex flex-col gap-4">
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
        <Form.Field {form} name="password">
          <Form.Control>
            {#snippet children({ props })}
              <Form.Label>{m.users_field_password()}</Form.Label>
              <Input {...props} type="password" autocomplete="new-password" bind:value={$formData.password} />
            {/snippet}
          </Form.Control>
          <Form.FieldErrors />
        </Form.Field>
        <Form.Field {form} name="confirmPassword">
          <Form.Control>
            {#snippet children({ props })}
              <Form.Label>{m.auth_confirm_password()}</Form.Label>
              <Input {...props} type="password" autocomplete="new-password" bind:value={$formData.confirmPassword} />
            {/snippet}
          </Form.Control>
          <Form.FieldErrors />
        </Form.Field>
        <Form.Field {form} name="registrationCode">
          <Form.Control>
            {#snippet children({ props })}
              <Form.Label>{m.auth_registration_code()}</Form.Label>
              <Input {...props} bind:value={$formData.registrationCode} />
            {/snippet}
          </Form.Control>
          <Form.FieldErrors />
        </Form.Field>
        {#if $message}
          <p role="alert" class="text-sm text-destructive">{$message}</p>
        {/if}
        <Form.Button disabled={$submitting} pending={$delayed}>{m.nav_register()}</Form.Button>
      </form>
    </Card.Content>
    <Card.Footer>
      <p class="text-sm text-muted-foreground">
        {m.auth_already_registered()}
        <a href={resolve('/(auth)/login')} class="underline">{m.nav_login()}</a>
      </p>
    </Card.Footer>
  </Card.Root>
</div>
