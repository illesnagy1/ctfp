<script lang="ts">
  import * as Tabs from '#lib/components/ui/tabs/index.js';
  import { Textarea } from '#lib/components/ui/textarea/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { cn } from '#lib/utils.js';

  import type { ControlAttributes } from './types.js';
  import WysiwygEditor from './wysiwyg-editor.svelte';

  let {
    value = $bindable(''),
    class: className,
    ...restProps
  }: {
    /** Markdown, as the api stores it and the server renders it. */
    value?: string;
    class?: string;
    // The form control's id and aria-* attributes, for whichever tab is open (only that one is rendered).
  } & ControlAttributes = $props();

  let tab = $state<'write' | 'preview'>('write');
</script>

<!-- Write edits the markdown itself; Preview shows it formatted, and can be edited there too. -->
<Tabs.Root bind:value={tab} class={cn('w-full gap-2', className)}>
  <Tabs.List>
    <Tabs.Trigger value="write">{m.editor_write()}</Tabs.Trigger>
    <Tabs.Trigger value="preview">{m.editor_preview()}</Tabs.Trigger>
  </Tabs.List>
  <Tabs.Content value="write">
    <Textarea {...restProps} class="min-h-32 font-mono" bind:value />
  </Tabs.Content>
  <Tabs.Content value="preview">
    <WysiwygEditor {...restProps} bind:value />
  </Tabs.Content>
</Tabs.Root>
