import { paraglideVitePlugin } from '@inlang/paraglide-js';
import adapter from '@sveltejs/adapter-node';
import { sveltekit } from '@sveltejs/kit/vite';
import tailwindcss from '@tailwindcss/vite';
import { playwright } from '@vitest/browser-playwright';
import { defineConfig } from 'vitest/config';

export default defineConfig({
  plugins: [
    tailwindcss(),
    sveltekit({
      compilerOptions: {
        // Force runes mode for the project, except for libraries. Can be removed in svelte 6.
        runes: ({ filename }) => (filename.split(/[/\\]/).includes('node_modules') ? undefined : true),
      },
      // Builds a standalone server into build/, run by the Containerfile with `bun index.js`.
      adapter: adapter(),
      // OpenTelemetry spans for handle, loads, actions and endpoints; exported only when src/instrumentation.server.ts
      // finds an OTLP endpoint.
      tracing: { server: true },
      // Public URL, baked in at build time; trusted origin for CSRF checks on form actions.
      // Unset: derived from the Host header with https assumed, so plain-http form posts get a 403.
      paths: { origin: process.env.ORIGIN || undefined },
      // Nonces for SSR pages, hashes for prerendered ones; the nonce reaches app.html's theme script too.
      // Inline styles stay allowed: components (bits-ui, the editor) set style attributes.
      csp: {
        mode: 'auto',
        directives: {
          'default-src': ['self'],
          'script-src': ['self'],
          'style-src': ['self', 'unsafe-inline'],
          // Challenge descriptions may embed images from other sites (sanitized markdown allows any https image).
          'img-src': ['self', 'data:', 'https:'],
          'font-src': ['self', 'data:'],
          'connect-src': ['self'],
          'object-src': ['none'],
          'base-uri': ['none'],
          'form-action': ['self'],
          // Nobody may frame the site: clickjacking on flag submission and admin actions.
          'frame-ancestors': ['none'],
        },
      },
    }),
    paraglideVitePlugin({ project: './project.inlang', outdir: './src/lib/paraglide' }),
  ],
  // The SJSF shadcn themes import tailwind-merge themselves; give them tailwind-variants' compatible twMerge,
  // so the bundle ships one merge engine.
  resolve: { alias: { 'tailwind-merge': 'tailwind-variants' } },
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
  ssr: { noExternal: ['@lucide/svelte'] },
});
