import { describe, expect, it } from 'vitest';
import { render } from 'vitest-browser-svelte';
import { userEvent } from 'vitest/browser';

import WysiwygEditor from './wysiwyg-editor.svelte';

/** Renders the editor with `value` bound, like `bind:value`; returns the screen and the current markdown. */
async function setup(initial: string) {
  const bound = { value: initial };
  const screen = await render(WysiwygEditor, {
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
  return { screen, bound };
}

describe('WysiwygEditor', () => {
  it('shows markdown as formatted text', async () => {
    const { screen } = await setup('## Rules\n\nFind the **flag**.');
    await expect.element(screen.getByRole('heading', { name: 'Rules' })).toBeVisible();
    await expect.element(screen.getByText('flag')).toHaveProperty('tagName', 'STRONG');
  });

  it('writes edits back as markdown', async () => {
    const { screen, bound } = await setup('Hello');
    await screen.getByRole('textbox', { name: 'Description' }).click();
    await userEvent.keyboard('{ControlOrMeta>}a{/ControlOrMeta}');
    await screen.getByRole('button', { name: 'Bold' }).click();
    await expect.poll(() => bound.value).toBe('**Hello**');
    await expect.element(screen.getByRole('button', { name: 'Bold' })).toHaveAttribute('aria-pressed', 'true');
  });

  it('links the selected text, and removes the link again', async () => {
    const { screen, bound } = await setup('Rules');
    await screen.getByRole('textbox', { name: 'Description' }).click();
    await userEvent.keyboard('{ControlOrMeta>}a{/ControlOrMeta}');
    await screen.getByRole('button', { name: 'Link' }).click();
    await screen.getByLabelText('URL').fill('example.com/rules');
    await screen.getByRole('button', { name: 'Insert' }).click();
    await expect.poll(() => bound.value).toBe('[Rules](https://example.com/rules)');

    await screen.getByRole('link', { name: 'Rules' }).click();
    await screen.getByRole('button', { name: 'Link' }).click();
    await screen.getByRole('button', { name: 'Remove link' }).click();
    await expect.poll(() => bound.value).toBe('Rules');
  });

  it("refuses links the page wouldn't keep", async () => {
    const { screen } = await setup('Rules');
    await screen.getByRole('textbox', { name: 'Description' }).click();
    await screen.getByRole('button', { name: 'Link' }).click();
    await screen.getByLabelText('URL').fill('javascript:alert(1)');
    await expect.element(screen.getByRole('button', { name: 'Insert' })).toBeDisabled();
  });
});
