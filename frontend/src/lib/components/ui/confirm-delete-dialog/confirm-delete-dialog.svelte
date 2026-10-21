<script lang="ts" module>
  import type { Snippet } from 'svelte';

  import { m } from '#lib/paraglide/messages.js';

  class ConfirmDeleteDialogState {
    open = $state(false);
    inputText = $state('');
    // Raw: holds callbacks and snippets, nothing to make deeply reactive.
    options = $state.raw<ConfirmDeleteOptions | null>(null);
    loading = $state(false);
    error = $state<string>();

    constructor() {
      this.confirm = this.confirm.bind(this);
      this.cancel = this.cancel.bind(this);
    }

    get matches() {
      return !this.options?.input || this.inputText === this.options.input.confirmationText;
    }

    newConfirmation(options: ConfirmDeleteOptions) {
      this.reset();
      this.options = options;
      this.open = true;
    }

    reset() {
      this.open = false;
      this.inputText = '';
      this.options = null;
      this.error = undefined;
    }

    async confirm() {
      if (!this.options || !this.matches || this.loading) return;

      this.loading = true;
      this.error = undefined;
      try {
        await this.options.onConfirm();
        this.open = false;
      } catch (error) {
        // Stay open so the user sees why and can retry.
        this.error = error instanceof Error ? error.message : m.confirm_delete_failed();
      } finally {
        this.loading = false;
      }
    }

    cancel() {
      this.options?.onCancel?.();
      this.open = false;
    }
  }

  const dialogState = new ConfirmDeleteDialogState();

  export type ConfirmDeleteOptions = {
    title: string;
    description: string;
    /** Extra content under the description, e.g. what else gets deleted with the item. */
    details?: Snippet;
    skipConfirmation?: boolean;
    /** Makes the user type this (e.g. the item's name) before the delete button unlocks. */
    input?: {
      confirmationText: string;
    };
    confirm?: {
      text?: string;
    };
    cancel?: {
      text?: string;
    };
    /** Does the delete. Throw to keep the dialog open and show the error's message. */
    onConfirm: () => Promise<unknown>;
    onCancel?: () => void;
  };

  /** Opens the dialog; `<ConfirmDeleteDialog />` must be mounted once (the root layout does). */
  export function confirmDelete(options: ConfirmDeleteOptions) {
    if (options.skipConfirmation) {
      void options.onConfirm();
      return;
    }

    dialogState.newConfirmation(options);
  }
</script>

<script lang="ts">
  import * as AlertDialog from '#lib/components/ui/alert-dialog/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import { Label } from '#lib/components/ui/label/index.js';
  import { Spinner } from '#lib/components/ui/spinner/index.js';

  const inputId = $props.id();
</script>

<AlertDialog.Root bind:open={dialogState.open}>
  <AlertDialog.Content>
    <form
      method="POST"
      onsubmit={(e) => {
        e.preventDefault();
        void dialogState.confirm();
      }}
      class="flex flex-col gap-4"
    >
      <AlertDialog.Header>
        <AlertDialog.Title>
          {dialogState.options?.title}
        </AlertDialog.Title>
        <AlertDialog.Description>
          {dialogState.options?.description}
        </AlertDialog.Description>
      </AlertDialog.Header>
      {@render dialogState.options?.details?.()}
      {#if dialogState.options?.input}
        <div class="flex flex-col gap-2">
          <Label for={inputId}>
            {m.confirm_delete_type_to_confirm({ text: dialogState.options.input.confirmationText })}
          </Label>
          <Input
            id={inputId}
            bind:value={dialogState.inputText}
            autocomplete="off"
            onkeydown={(e) => {
              if (e.key === 'Enter') {
                // for some reason without this the form will submit and the dialog will close immediately
                e.preventDefault();
                void dialogState.confirm();
              }
            }}
          />
        </div>
      {/if}
      {#if dialogState.error}
        <p role="alert" class="text-sm text-destructive">{dialogState.error}</p>
      {/if}
      <AlertDialog.Footer>
        <AlertDialog.Cancel type="button" onclick={dialogState.cancel} disabled={dialogState.loading}>
          {dialogState.options?.cancel?.text ?? m.confirm_delete_cancel()}
        </AlertDialog.Cancel>
        <AlertDialog.Action type="submit" variant="destructive" disabled={dialogState.loading || !dialogState.matches}>
          {#if dialogState.loading}<Spinner />{/if}
          {dialogState.options?.confirm?.text ?? m.confirm_delete_confirm()}
        </AlertDialog.Action>
      </AlertDialog.Footer>
    </form>
  </AlertDialog.Content>
</AlertDialog.Root>
