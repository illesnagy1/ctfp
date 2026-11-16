<script lang="ts">
  import { resolve } from '$app/paths';
  import { superForm } from 'sveltekit-superforms';
  import { zod4Client } from 'sveltekit-superforms/adapters';

  import * as Card from '#lib/components/ui/card/index.js';
  import * as Form from '#lib/components/ui/form/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { forgotPasswordSchema } from '#lib/schemas/auth.js';

  let { data } = $props();

  // Created once from the initial data; superForm keeps it up to date itself.
  // svelte-ignore state_referenced_locally
  const form = superForm(data.form, { validators: zod4Client(forgotPasswordSchema) });
  const { form: formData, enhance, message, submitting, delayed } = form;
</script>

<div class="mx-auto max-w-sm px-4 py-12">
  <Card.Root>
    <Card.Header>
      <Card.Title>{m.forgot_password_title()}</Card.Title>
      <Card.Description>{m.forgot_password_description()}</Card.Description>
    </Card.Header>
    <Card.Content>
      {#if data.sent}
        <p role="status" class="text-sm">{m.forgot_password_sent()}</p>
      {:else}
        <form method="post" use:enhance class="flex flex-col gap-4">
          <Form.Field {form} name="email">
            <Form.Control>
              {#snippet children({ props })}
                <Form.Label>{m.users_column_email()}</Form.Label>
                <Input {...props} type="email" autocomplete="email" bind:value={$formData.email} />
              {/snippet}
            </Form.Control>
            <Form.FieldErrors />
          </Form.Field>
          {#if $message}
            <p role="alert" class="text-sm text-destructive">{$message}</p>
          {/if}
          <Form.Button disabled={$submitting} pending={$delayed}>{m.forgot_password_submit()}</Form.Button>
        </form>
      {/if}
    </Card.Content>
    <Card.Footer>
      <a href={resolve('/(auth)/login')} class="text-sm underline">{m.nav_login()}</a>
    </Card.Footer>
  </Card.Root>
</div>
