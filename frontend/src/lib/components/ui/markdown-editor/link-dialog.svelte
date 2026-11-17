<script lang="ts">
  import { Button } from '#lib/components/ui/button/index.js';
  import * as Dialog from '#lib/components/ui/dialog/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import { Label } from '#lib/components/ui/label/index.js';
  import { m } from '#lib/paraglide/messages.js';

  import { normalizeHref } from './link.js';

  let {
    open = $bindable(false),
    href = $bindable(''),
    text = $bindable(''),
    askText,
    editing,
    onsave,
    onremove,
  }: {
    open?: boolean;
    href?: string;
    /** What the new link says; only asked for when no text is selected. */
    text?: string;
    askText: boolean;
    /** Changing an existing link, which can then also be removed. */
    editing: boolean;
    onsave: (href: string, text: string) => void;
    onremove: () => void;
  } = $props();

  const normalized = $derived(normalizeHref(href));
  // Only once something was typed, so an empty field doesn't start out red.
  const invalid = $derived(href.trim() !== '' && normalized === undefined);

  function save(event: SubmitEvent) {
    // Portaled to <body>, so this form isn't nested in the one the editor sits in; it never submits anywhere.
    event.preventDefault();
    if (!normalized) return;
    onsave(normalized, text.trim());
    open = false;
  }

  function remove() {
    onremove();
    open = false;
  }
</script>

<Dialog.Root bind:open>
  <Dialog.Content class="sm:max-w-md">
    <Dialog.Header>
      <Dialog.Title>{editing ? m.editor_link_edit() : m.editor_link()}</Dialog.Title>
      <Dialog.Description>{m.editor_link_description()}</Dialog.Description>
    </Dialog.Header>
    <form class="flex flex-col gap-4" onsubmit={save}>
      {#if askText}
        <div class="flex flex-col gap-2">
          <Label for="link-text">{m.editor_link_text()}</Label>
          <Input id="link-text" bind:value={text} />
        </div>
      {/if}
      <div class="flex flex-col gap-2">
        <Label for="link-href">{m.editor_link_url()}</Label>
        <Input
          id="link-href"
          type="text"
          inputmode="url"
          placeholder="https://example.com"
          aria-invalid={invalid}
          aria-describedby={invalid ? 'link-href-error' : undefined}
          bind:value={href}
        />
        {#if invalid}
          <p id="link-href-error" class="text-sm text-destructive">{m.editor_link_invalid()}</p>
        {/if}
      </div>
      <Dialog.Footer>
        {#if editing}
          <Button type="button" variant="destructive" class="sm:mr-auto" onclick={remove}>
            {m.editor_link_remove()}
          </Button>
        {/if}
        <Button type="button" variant="outline" onclick={() => (open = false)}>{m.editor_cancel()}</Button>
        <Button type="submit" disabled={!normalized}>{editing ? m.editor_update() : m.editor_insert()}</Button>
      </Dialog.Footer>
    </form>
  </Dialog.Content>
</Dialog.Root>
