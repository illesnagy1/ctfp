// ESLint runs @shadcn/lint and markup-aware Svelte rules; everything else is linted by oxlint (.oxlintrc.json).
// Oxlint can't parse Svelte markup, so the design-system rules need ESLint to see `class="..."`.
import { plugin as shadcn } from '@shadcn/lint';
import tsParser from '@typescript-eslint/parser';
import svelte from 'eslint-plugin-svelte';
import { defineConfig, globalIgnores } from 'eslint/config';
import svelteParser from 'svelte-eslint-parser';
import tseslint from 'typescript-eslint';

/** @type {import('eslint').Linter.RulesRecord} */
// https://github.com/shadcn-ui/lint/blob/main/README.md#rules
const rules = {
  'shadcn/no-restyle': ['error', { allow: ['layout'] }],
  'shadcn/no-raw-colors': 'error',
  'shadcn/no-arbitrary-values': 'error',
  'shadcn/no-inline-styles': 'error',
  'shadcn/no-unknown-classes': 'error',
  'shadcn/require-static-classes': 'error',
};

export default defineConfig([
  globalIgnores(['.svelte-kit/', 'build/', 'node_modules/', 'src/lib/paraglide/', 'project.inlang/cache/']),
  {
    // Generated code has `eslint-disable` comments for oxlint-covered rules. Register those plugins (no rules
    // enabled) so the comments resolve, and don't report them as unused.
    plugins: { '@typescript-eslint': tseslint.plugin, svelte },
    linterOptions: { reportUnusedDisableDirectives: 'off' },
  },
  {
    files: ['**/*.svelte'],
    languageOptions: {
      parser: svelteParser,
      parserOptions: { parser: tsParser },
    },
    plugins: { shadcn },
    // Applies the markup disable comments that `svelte/comment-directive` collects.
    processor: 'svelte/svelte',
    rules: {
      ...rules,
      // Oxlint only sees the <script> block, so it misses reassignments in markup (`bind:`, `onclick`).
      'svelte/prefer-const': 'error',
      // Makes `<!-- eslint-disable-next-line … -->` comments in markup work.
      'svelte/comment-directive': 'error',
    },
  },
  {
    // `cn` / `tv` calls outside components, e.g. table column definitions.
    files: ['**/*.{js,ts}'],
    languageOptions: { parser: tsParser },
    plugins: { shadcn },
    rules,
  },
  {
    // The design system itself: components style their own parts, define the off-scale values, and compose variants.
    files: ['src/lib/components/ui/**'],
    rules: {
      'shadcn/no-restyle': 'off',
      'shadcn/no-arbitrary-values': 'off',
      'shadcn/require-static-classes': 'off',
    },
  },
]);
