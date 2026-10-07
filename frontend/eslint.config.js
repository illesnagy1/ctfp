// ESLint runs only @shadcn/lint; everything else is linted by oxlint (.oxlintrc.json).
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
    rules,
  },
  {
    // `cn` / `tv` calls outside components, e.g. table column definitions.
    files: ['**/*.{js,ts}'],
    languageOptions: { parser: tsParser },
    plugins: { shadcn },
    rules,
  },
  {
    // The design system itself: components style their own parts.
    files: ['src/lib/components/ui/**'],
    rules: { 'shadcn/no-restyle': 'off' },
  },
]);
