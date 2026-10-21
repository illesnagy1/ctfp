import { describe, expect, it } from 'vitest';

import { renderMarkdown } from './markdown';

describe('renderMarkdown', () => {
  it('renders markdown and GitHub extensions', async () => {
    const html = await renderMarkdown('# Title\n\n*em* and ~~gone~~\n\n| a |\n| - |\n| b |');
    expect(html).toContain('<em>em</em>');
    expect(html).toContain('<del>gone</del>');
    expect(html).toContain('<table>');
  });

  it('keeps safe inline html', async () => {
    expect(await renderMarkdown('<kbd>Ctrl</kbd>')).toContain('<kbd>Ctrl</kbd>');
  });

  it.each([
    ['script tags', '<script>alert(1)</script>', '<script'],
    ['event handlers', '<img src="x" onerror="alert(1)">', 'onerror'],
    ['javascript: links', '[click](javascript:alert(1))', 'javascript:'],
    ['raw javascript: links', '<a href="javascript:alert(1)">click</a>', 'javascript:'],
    ['iframes', '<iframe src="https://example.com"></iframe>', '<iframe'],
    ['inline styles', '<p style="position:fixed">x</p>', 'style='],
  ])('strips %s', async (_, markdown, forbidden) => {
    expect(await renderMarkdown(markdown)).not.toContain(forbidden);
  });
});
