<script lang="ts">
  import { resolve } from '$app/paths';
  import { superForm } from 'sveltekit-superforms';
  import { zod4Client } from 'sveltekit-superforms/adapters';

  import * as Card from '#lib/components/ui/card/index.js';
  import * as Form from '#lib/components/ui/form/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import { loginSchema } from '#lib/schemas/auth.js';

  let { data } = $props();

  // Created once from the initial data; superForm keeps it up to date itself.
  // svelte-ignore state_referenced_locally
  const form = superForm(data.form, { validators: zod4Client(loginSchema) });
  const { form: formData, enhance, message, submitting } = form;
</script>

<div class="mx-auto max-w-sm px-4 py-12">
  <Card.Root>
    <Card.Header>
      <Card.Title>Login</Card.Title>
    </Card.Header>
    <Card.Content>
      <form method="post" use:enhance class="flex flex-col gap-4">
        <Form.Field {form} name="login">
          <Form.Control>
            {#snippet children({ props })}
              <Form.Label>Username or email</Form.Label>
              <Input {...props} autocomplete="username" bind:value={$formData.login} />
            {/snippet}
          </Form.Control>
          <Form.FieldErrors />
        </Form.Field>
        <Form.Field {form} name="password">
          <Form.Control>
            {#snippet children({ props })}
              <Form.Label>Password</Form.Label>
              <Input {...props} type="password" autocomplete="current-password" bind:value={$formData.password} />
            {/snippet}
          </Form.Control>
          <Form.FieldErrors />
        </Form.Field>
        {#if $message}
          <p role="alert" class="text-sm text-destructive">{$message}</p>
        {/if}
        <Form.Button disabled={$submitting}>Login</Form.Button>
      </form>
    </Card.Content>
    <Card.Footer>
      <p class="text-sm text-muted-foreground">
        No account?
        <a href={resolve('register')} class="underline">Register</a>
      </p>
    </Card.Footer>
  </Card.Root>
</div>
