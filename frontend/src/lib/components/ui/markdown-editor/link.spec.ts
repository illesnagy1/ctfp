import { describe, expect, it } from 'vitest';

import { normalizeHref } from './link';

describe('normalizeHref', () => {
  it.each([
    ['https://example.com/a?b#c', 'https://example.com/a?b#c'],
    ['  http://example.com ', 'http://example.com'],
    ['example.com/rules', 'https://example.com/rules'],
    ['mailto:admin@example.com', 'mailto:admin@example.com'],
    ['/challenges/2', '/challenges/2'],
    ['#flag-format', '#flag-format'],
  ])('keeps %s', (input, href) => {
    expect(normalizeHref(input)).toBe(href);
  });

  it.each([
    '',
    '   ',
    'javascript:alert(1)',
    'JavaScript:alert(1)',
    'data:text/html,x',
    'ftp://example.com',
    'two words',
    'https://',
  ])('refuses %j', (input) => {
    expect(normalizeHref(input)).toBeUndefined();
  });
});
