<script lang="ts">
  import { resolve } from '$app/paths';
  import { superForm } from 'sveltekit-superforms';
  import { zod4Client } from 'sveltekit-superforms/adapters';

  import * as Card from '#lib/components/ui/card/index.js';
  import * as Form from '#lib/components/ui/form/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { resetPasswordSchema } from '#lib/schemas/auth.js';

  let { data } = $props();

  // Created once from the initial data; superForm keeps it up to date itself.
  // svelte-ignore state_referenced_locally
  const form = superForm(data.form, { validators: zod4Client(resetPasswordSchema) });
  const { form: formData, enhance, message, submitting, delayed } = form;
</script>

<svelte:head>
  <!-- The token is in this page's URL; don't hand it to other sites as a referrer. -->
  <meta name="referrer" content="no-referrer" />
</svelte:head>

<div class="mx-auto max-w-sm px-4 py-12">
  <Card.Root>
    <Card.Header>
      <Card.Title>{m.reset_password_title()}</Card.Title>
    </Card.Header>
    <Card.Content>
      {#if data.hasToken}
        <form method="post" use:enhance class="flex flex-col gap-4">
          <input type="hidden" name="token" value={$formData.token} />
          <Form.Field {form} name="password">
            <Form.Control>
              {#snippet children({ props })}
                <Form.Label>{m.reset_password_new()}</Form.Label>
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
          {#if $message}
            <p role="alert" class="text-sm text-destructive">{$message}</p>
          {/if}
          <Form.Button disabled={$submitting} pending={$delayed}>{m.reset_password_submit()}</Form.Button>
        </form>
      {:else}
        <p role="alert" class="text-sm">{m.reset_password_missing_token()}</p>
      {/if}
    </Card.Content>
    <Card.Footer>
      <a href={resolve('/(auth)/forgot-password')} class="text-sm underline">{m.forgot_password_title()}</a>
    </Card.Footer>
  </Card.Root>
</div>
