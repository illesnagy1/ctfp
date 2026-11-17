import { describe, expect, it } from 'vitest';
import { render } from 'vitest-browser-svelte';
import { userEvent } from 'vitest/browser';

import MarkdownEditor from './markdown-editor.svelte';

describe('MarkdownEditor', () => {
  it('edits the markdown in Write and the formatted text in Preview', async () => {
    const bound = $state({ value: '' });
    const screen = await render(MarkdownEditor, {
      props: {
        get value() {
          return bound.value;
        },
        set value(value: string) {
          bound.value = value;
        },
        'aria-label': 'Description',
      },
    });

    await screen.getByRole('textbox', { name: 'Description' }).fill('## Rules\n\nFind the flag.');
    expect(bound.value).toBe('## Rules\n\nFind the flag.');

    await screen.getByRole('tab', { name: 'Preview' }).click();
    await expect.element(screen.getByRole('heading', { name: 'Rules' })).toBeVisible();
    await screen.getByText('Find the flag.').click();
    await userEvent.keyboard('{End} Quickly.');
    await expect.poll(() => bound.value).toBe('## Rules\n\nFind the flag. Quickly.');

    await screen.getByRole('tab', { name: 'Write' }).click();
    await expect
      .element(screen.getByRole('textbox', { name: 'Description' }))
      .toHaveValue('## Rules\n\nFind the flag. Quickly.');
  });
});
