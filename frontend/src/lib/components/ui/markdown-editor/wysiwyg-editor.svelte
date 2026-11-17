<script lang="ts">
  import BoldIcon from '@lucide/svelte/icons/bold';
  import ChevronDownIcon from '@lucide/svelte/icons/chevron-down';
  import CodeIcon from '@lucide/svelte/icons/code';
  import Heading1Icon from '@lucide/svelte/icons/heading-1';
  import Heading2Icon from '@lucide/svelte/icons/heading-2';
  import Heading3Icon from '@lucide/svelte/icons/heading-3';
  import Heading4Icon from '@lucide/svelte/icons/heading-4';
  import ItalicIcon from '@lucide/svelte/icons/italic';
  import LinkIcon from '@lucide/svelte/icons/link';
  import ListIcon from '@lucide/svelte/icons/list';
  import ListOrderedIcon from '@lucide/svelte/icons/list-ordered';
  import PilcrowIcon from '@lucide/svelte/icons/pilcrow';
  import QuoteIcon from '@lucide/svelte/icons/quote';
  import SquareCodeIcon from '@lucide/svelte/icons/square-code';
  import StrikethroughIcon from '@lucide/svelte/icons/strikethrough';
  import { Editor } from '@tiptap/core';
  import { onMount, type Component } from 'svelte';

  import { Button, buttonVariants } from '#lib/components/ui/button/index.js';
  import * as DropdownMenu from '#lib/components/ui/dropdown-menu/index.js';
  import { Separator } from '#lib/components/ui/separator/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { cn } from '#lib/utils.js';

  import { editorExtensions, headingLevels } from './extensions.js';
  import LinkDialog from './link-dialog.svelte';
  import type { ControlAttributes } from './types.js';

  let {
    value = $bindable(''),
    class: className,
    ...restProps
  }: {
    /** Markdown, as the api stores it and the server renders it. */
    value?: string;
    class?: string;
    // The form control's id and aria-* attributes, put on the editable area so labels and errors point at it.
  } & ControlAttributes = $props();

  let element = $state<HTMLDivElement>();
  let editor = $state.raw<Editor>();
  // Bumped on every transaction, so the toolbar re-renders its active states; the editor itself isn't reactive.
  let revision = $state(0);
  // The markdown last written to `value`, to tell the editor's own changes from outside ones (e.g. a form reset).
  let emitted = value;

  // The control's attributes as ProseMirror takes them (strings only), on the editable area.
  const attributes = $derived({
    ...Object.fromEntries(
      Object.entries(restProps).flatMap(([key, v]) => (v == null || v === false ? [] : [[key, String(v)]])),
    ),
    role: 'textbox',
    'aria-multiline': 'true',
    class: 'prose prose-sm max-w-none min-h-24 px-2.5 py-2 outline-none dark:prose-invert',
  });

  // The link being added or, inside an existing one, edited.
  let link = $state({ open: false, href: '', text: '', askText: false, editing: false });

  function openLink() {
    if (!editor) return;
    const href: unknown = editor.getAttributes('link').href;
    const editing = typeof href === 'string';
    link = {
      open: true,
      href: editing ? href : '',
      text: '',
      askText: !editing && editor.state.selection.empty,
      editing,
    };
  }

  function saveLink(href: string, text: string) {
    if (!editor) return;
    if (link.askText) {
      // Nothing selected: insert the text (or the address itself) as the link.
      editor
        .chain()
        .focus()
        .insertContent({ type: 'text', text: text || href, marks: [{ type: 'link', attrs: { href } }] })
        .run();
    } else {
      editor.chain().focus().extendMarkRange('link').setLink({ href }).run();
    }
  }

  function removeLink() {
    editor?.chain().focus().extendMarkRange('link').unsetLink().run();
  }

  onMount(() => {
    const instance = new Editor({
      element,
      extensions: editorExtensions(),
      content: value,
      contentType: 'markdown',
      editorProps: { attributes },
      onTransaction: () => {
        revision += 1;
      },
      onUpdate: () => {
        emitted = instance.getMarkdown();
        value = emitted;
      },
    });
    editor = instance;
    return () => instance.destroy();
  });

  $effect(() => {
    if (editor && value !== emitted) {
      emitted = value;
      editor.commands.setContent(value, { contentType: 'markdown', emitUpdate: false });
    }
  });

  // Keeps aria-invalid and aria-describedby current as the form validates.
  $effect(() => {
    editor?.setOptions({ editorProps: { attributes } });
  });

  type Tool = {
    label: () => string;
    icon: Component;
    active?: (editor: Editor) => boolean;
    run: (editor: Editor) => void;
  };

  const marks: Tool[] = [
    {
      label: m.editor_bold,
      icon: BoldIcon,
      active: (e) => e.isActive('bold'),
      run: (e) => e.chain().focus().toggleBold().run(),
    },
    {
      label: m.editor_italic,
      icon: ItalicIcon,
      active: (e) => e.isActive('italic'),
      run: (e) => e.chain().focus().toggleItalic().run(),
    },
    {
      label: m.editor_strike,
      icon: StrikethroughIcon,
      active: (e) => e.isActive('strike'),
      run: (e) => e.chain().focus().toggleStrike().run(),
    },
    {
      label: m.editor_code,
      icon: CodeIcon,
      active: (e) => e.isActive('code'),
      run: (e) => e.chain().focus().toggleCode().run(),
    },
    { label: m.editor_link, icon: LinkIcon, active: (e) => e.isActive('link'), run: openLink },
  ];

  const blocks: Tool[] = [
    {
      label: m.editor_bullet_list,
      icon: ListIcon,
      active: (e) => e.isActive('bulletList'),
      run: (e) => e.chain().focus().toggleBulletList().run(),
    },
    {
      label: m.editor_ordered_list,
      icon: ListOrderedIcon,
      active: (e) => e.isActive('orderedList'),
      run: (e) => e.chain().focus().toggleOrderedList().run(),
    },
    {
      label: m.editor_quote,
      icon: QuoteIcon,
      active: (e) => e.isActive('blockquote'),
      run: (e) => e.chain().focus().toggleBlockquote().run(),
    },
    {
      label: m.editor_code_block,
      icon: SquareCodeIcon,
      active: (e) => e.isActive('codeBlock'),
      run: (e) => e.chain().focus().toggleCodeBlock().run(),
    },
  ];

  const headingIcons = { 1: Heading1Icon, 2: Heading2Icon, 3: Heading3Icon, 4: Heading4Icon };
  const blockTypes = [
    { value: 'paragraph', label: m.editor_paragraph, icon: PilcrowIcon },
    ...headingLevels.map((level) => ({
      value: `h${level}`,
      label: () => m.editor_heading_level({ level }),
      icon: headingIcons[level],
    })),
  ];
  // What applies at the cursor, recomputed on every transaction.
  const current = $derived.by(() => {
    void revision;
    const level = editor && headingLevels.find((l) => editor?.isActive('heading', { level: l }));
    return {
      active: new Set([...marks, ...blocks].filter((tool) => editor && tool.active?.(editor))),
      blockType: blockTypes.find((type) => type.value === (level ? `h${level}` : 'paragraph')) ?? blockTypes[0],
    };
  });

  function setBlockType(type: string) {
    const level = headingLevels.find((l) => type === `h${l}`);
    if (level) editor?.chain().focus().setHeading({ level }).run();
    else editor?.chain().focus().setParagraph().run();
  }
</script>

{#snippet toolButton(tool: Tool)}
  {@const active = current.active.has(tool)}
  <!-- type="button": inside a form, a plain button would submit it. -->
  <Button
    type="button"
    size="icon-sm"
    variant={active ? 'secondary' : 'ghost'}
    aria-label={tool.label()}
    title={tool.label()}
    aria-pressed={tool.active ? active : undefined}
    disabled={!editor}
    onclick={() => editor && tool.run(editor)}
  >
    <tool.icon />
  </Button>
{/snippet}

{#snippet divider()}
  <Separator orientation="vertical" class="mx-0.5 h-6 self-center" />
{/snippet}

<div
  data-slot="markdown-editor"
  aria-invalid={restProps['aria-invalid']}
  class={cn(
    'w-full rounded-md border border-input bg-transparent shadow-xs transition-[color,box-shadow] focus-within:border-ring focus-within:ring-3 focus-within:ring-ring/50 aria-invalid:border-destructive aria-invalid:ring-3 aria-invalid:ring-destructive/20 dark:bg-input/30 dark:aria-invalid:border-destructive/50 dark:aria-invalid:ring-destructive/40',
    className,
  )}
>
  <div
    role="toolbar"
    aria-label={m.editor_toolbar()}
    class="flex flex-wrap items-center gap-0.5 border-b border-input p-1"
  >
    <DropdownMenu.Root>
      <DropdownMenu.Trigger
        class={buttonVariants({ variant: 'ghost', size: 'sm' })}
        disabled={!editor}
        aria-label={m.editor_block_type()}
        title={m.editor_block_type()}
      >
        <current.blockType.icon />
        <ChevronDownIcon class="text-muted-foreground" />
      </DropdownMenu.Trigger>
      <DropdownMenu.Content align="start">
        <DropdownMenu.RadioGroup value={current.blockType.value} onValueChange={setBlockType}>
          {#each blockTypes as type (type.value)}
            <DropdownMenu.RadioItem value={type.value}><type.icon />{type.label()}</DropdownMenu.RadioItem>
          {/each}
        </DropdownMenu.RadioGroup>
      </DropdownMenu.Content>
    </DropdownMenu.Root>
    {@render divider()}
    {#each marks as tool (tool.label)}
      {@render toolButton(tool)}
    {/each}
    {@render divider()}
    {#each blocks as tool (tool.label)}
      {@render toolButton(tool)}
    {/each}
  </div>
  <div bind:this={element}></div>
</div>

<LinkDialog
  bind:open={link.open}
  bind:href={link.href}
  bind:text={link.text}
  askText={link.askText}
  editing={link.editing}
  onsave={saveLink}
  onremove={removeLink}
/>
