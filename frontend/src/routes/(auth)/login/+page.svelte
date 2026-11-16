<script lang="ts">
  import { resolve } from '$app/paths';
  import { superForm } from 'sveltekit-superforms';
  import { zod4Client } from 'sveltekit-superforms/adapters';

  import * as Card from '#lib/components/ui/card/index.js';
  import * as Form from '#lib/components/ui/form/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { loginSchema } from '#lib/schemas/auth.js';

  let { data } = $props();

  // Created once from the initial data; superForm keeps it up to date itself.
  // svelte-ignore state_referenced_locally
  const form = superForm(data.form, { validators: zod4Client(loginSchema) });
  const { form: formData, enhance, message, submitting, delayed } = form;
</script>

<div class="mx-auto max-w-sm px-4 py-12">
  <Card.Root>
    <Card.Header>
      <Card.Title>{m.nav_login()}</Card.Title>
    </Card.Header>
    <Card.Content>
      {#if data.passwordReset}
        <p role="status" class="pb-4 text-sm">{m.reset_password_done()}</p>
      {/if}
      <form method="post" use:enhance class="flex flex-col gap-4">
        <Form.Field {form} name="login">
          <Form.Control>
            {#snippet children({ props })}
              <Form.Label>{m.auth_login_field()}</Form.Label>
              <Input {...props} autocomplete="username" bind:value={$formData.login} />
            {/snippet}
          </Form.Control>
          <Form.FieldErrors />
        </Form.Field>
        <Form.Field {form} name="password">
          <Form.Control>
            {#snippet children({ props })}
              <Form.Label>{m.users_field_password()}</Form.Label>
              <Input {...props} type="password" autocomplete="current-password" bind:value={$formData.password} />
            {/snippet}
          </Form.Control>
          <Form.FieldErrors />
        </Form.Field>
        <a href={resolve('/(auth)/forgot-password')} class="-mt-2 self-end text-sm text-muted-foreground underline">
          {m.auth_forgot_password()}
        </a>
        {#if $message}
          <p role="alert" class="text-sm text-destructive">{$message}</p>
        {/if}
        <Form.Button disabled={$submitting} pending={$delayed}>{m.nav_login()}</Form.Button>
      </form>
    </Card.Content>
    <Card.Footer>
      <p class="text-sm text-muted-foreground">
        {m.auth_no_account()}
        <a href={resolve('/(auth)/register')} class="underline">{m.nav_register()}</a>
      </p>
    </Card.Footer>
  </Card.Root>
</div>
