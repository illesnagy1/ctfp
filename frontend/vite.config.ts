import { paraglideVitePlugin } from '@inlang/paraglide-js';
import adapter from '@sveltejs/adapter-node';
import { sveltekit } from '@sveltejs/kit/vite';
import tailwindcss from '@tailwindcss/vite';
import { playwright } from '@vitest/browser-playwright';
import { mdsvex } from 'mdsvex';
import { defineConfig } from 'vitest/config';

export default defineConfig({
  plugins: [
    tailwindcss(),
    sveltekit({
      compilerOptions: {
        // Force runes mode for the project, except for libraries. Can be removed in svelte 6.
        runes: ({ filename }) => (filename.split(/[/\\]/).includes('node_modules') ? undefined : true),
      },
      preprocess: [mdsvex({ extensions: ['.svx', '.md'] })],
      extensions: ['.svelte', '.svx', '.md'],
      // Builds a standalone server into build/, run by the Containerfile with `bun index.js`.
      adapter: adapter(),
      // Public URL, baked in at build time; trusted origin for CSRF checks on form actions.
      // Unset: derived from the Host header with https assumed, so plain-http form posts get a 403.
      paths: { origin: process.env.ORIGIN || undefined },
    }),
    paraglideVitePlugin({ project: './project.inlang', outdir: './src/lib/paraglide' }),
  ],
  test: {
    expect: { requireAssertions: true },
    projects: [
      {
        extends: './vite.config.ts',
        test: {
          name: 'client',
          browser: {
            enabled: true,
            provider: playwright(),
            instances: [{ browser: 'chromium', headless: true }],
          },
          include: ['src/**/*.svelte.{test,spec}.{js,ts}'],
          exclude: ['src/lib/server/**'],
        },
      },

      {
        extends: './vite.config.ts',
        test: {
          name: 'server',
          environment: 'node',
          include: ['src/**/*.{test,spec}.{js,ts}'],
          exclude: ['src/**/*.svelte.{test,spec}.{js,ts}'],
        },
      },
    ],
  },
});
