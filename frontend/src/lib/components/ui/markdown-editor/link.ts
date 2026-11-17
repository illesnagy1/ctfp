/**
 * The link target for what was typed, or undefined if it isn't one the challenge page keeps: http(s), mailto, or a
 * path or anchor on this site (the server's sanitizer drops the rest, e.g. `javascript:`). `example.com` means
 * https://example.com.
 */
export function normalizeHref(input: string): string | undefined {
  const href = input.trim();
  if (!href || /\s/.test(href)) return undefined;
  if (/^(https?:\/\/[^/]|mailto:.|[/#])/i.test(href)) return href;
  // Any other scheme (javascript:, data:, ftp:) is refused rather than turned into a broken https link.
  if (/^[a-z][a-z\d+.-]*:/i.test(href)) return undefined;
  return `https://${href}`;
}
