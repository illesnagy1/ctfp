import { Markdown } from '@tiptap/markdown';
import { StarterKit } from '@tiptap/starter-kit';

// Descriptions are stored as markdown and rendered by the server (src/lib/server/markdown.ts), so the editor only
// offers what markdown (with GitHub's strikethrough) can express.

export const headingLevels = [1, 2, 3, 4] as const;

export const editorExtensions = () => [
  StarterKit.configure({
    // Markdown has no underline, so it wouldn't survive saving.
    underline: false,
    heading: { levels: [...headingLevels] },
    // Links stay editable text while editing; a typed or pasted `example.com` becomes https://example.com.
    link: { openOnClick: false, defaultProtocol: 'https' },
  }),
  Markdown,
];
