<script lang="ts">
  import { superForm, type Infer, type SuperValidated } from 'sveltekit-superforms';
  import { zod4Client } from 'sveltekit-superforms/adapters';

  import * as AlertDialog from '#lib/components/ui/alert-dialog/index.js';
  import { Button } from '#lib/components/ui/button/index.js';
  import * as Form from '#lib/components/ui/form/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { deleteAccountSchema } from '#lib/schemas/user.js';

  let { data }: { data: SuperValidated<Infer<typeof deleteAccountSchema>> } = $props();

  let open = $state(false);

  // Created once from the initial data; superForm keeps it up to date itself.
  // svelte-ignore state_referenced_locally
  const form = superForm(data, {
    validators: zod4Client(deleteAccountSchema),
    resetForm: false,
    onError({ result }) {
      form.message.set(result.error.message);
    },
  });
  const { form: formData, enhance, submitting, delayed, message } = form;

  function onOpenChange(value: boolean) {
    if (value) form.reset();
  }
</script>

<section class="flex flex-col gap-3 rounded-md border border-destructive/50 p-4" aria-labelledby="delete-account-title">
  <h2 id="delete-account-title" class="text-sm font-medium text-destructive">{m.account_delete_title()}</h2>
  <p class="text-sm text-muted-foreground">{m.account_delete_description()}</p>
  <AlertDialog.Root bind:open {onOpenChange}>
    <AlertDialog.Trigger>
      {#snippet child({ props })}
        <Button {...props} variant="destructive" class="self-start">{m.account_delete_title()}</Button>
      {/snippet}
    </AlertDialog.Trigger>
    <AlertDialog.Content>
      <form method="post" action="?/deleteAccount" class="flex flex-col gap-4" use:enhance>
        <AlertDialog.Header>
          <AlertDialog.Title>{m.account_delete_title()}</AlertDialog.Title>
          <AlertDialog.Description>{m.account_delete_confirm_description()}</AlertDialog.Description>
        </AlertDialog.Header>
        <Form.Field {form} name="password">
          <Form.Control>
            {#snippet children({ props })}
              <Form.Label>{m.account_delete_password()}</Form.Label>
              <Input {...props} type="password" autocomplete="current-password" bind:value={$formData.password} />
            {/snippet}
          </Form.Control>
          <Form.FieldErrors />
        </Form.Field>
        {#if $message}<p role="alert" class="text-sm font-medium text-destructive">{$message}</p>{/if}
        <AlertDialog.Footer>
          <AlertDialog.Cancel type="button" disabled={$submitting}>{m.confirm_delete_cancel()}</AlertDialog.Cancel>
          <Form.Button variant="destructive" disabled={$submitting} pending={$delayed}>
            {m.account_delete_confirm()}
          </Form.Button>
        </AlertDialog.Footer>
      </form>
    </AlertDialog.Content>
  </AlertDialog.Root>
</section>
