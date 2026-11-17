import { Editor } from '@tiptap/core';
import { describe, expect, it } from 'vitest';

import { editorExtensions } from './extensions';

// In the browser project (`.svelte.spec`): ProseMirror needs a DOM.
const roundTrip = (markdown: string) => {
  const editor = new Editor({ extensions: editorExtensions(), content: markdown, contentType: 'markdown' });
  try {
    return editor.getMarkdown();
  } finally {
    editor.destroy();
  }
};

describe('editor markdown', () => {
  it.each([
    ['emphasis, strikethrough and code', '**bold**, *italic*, ~~gone~~ and `code`'],
    ['heading levels', '# One\n\n## Two\n\n### Three\n\n#### Four'],
    ['links', 'See [the rules](https://example.com/rules) and [admins](mailto:admin@example.com).'],
  ])('keeps %s', (_, markdown) => {
    expect(roundTrip(markdown)).toBe(markdown);
  });

  it('drops what markdown has no syntax for', () => {
    expect(roundTrip('<u>under</u> x<sup>2</sup>')).toBe('under x2');
  });
});
