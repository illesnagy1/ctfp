import rehypeRaw from 'rehype-raw';
import rehypeSanitize from 'rehype-sanitize';
import rehypeStringify from 'rehype-stringify';
import remarkGfm from 'remark-gfm';
import remarkParse from 'remark-parse';
import remarkRehype from 'remark-rehype';
import { unified } from 'unified';

// Markdown with inline HTML allowed, then cleaned against GitHub's allowlist (rehype-sanitize's default schema):
// no scripts, styles, iframes, event handlers or `javascript:` urls. Sanitizing must stay the last step before
// stringifying, so no later plugin can add unchecked markup.
const processor = unified()
  .use(remarkParse)
  .use(remarkGfm)
  .use(remarkRehype, { allowDangerousHtml: true })
  .use(rehypeRaw)
  .use(rehypeSanitize)
  .use(rehypeStringify)
  .freeze();

/** Renders user-written markdown (and inline HTML) to HTML that is safe for `{@html}`. */
export async function renderMarkdown(markdown: string) {
  return String(await processor.process(markdown));
}
