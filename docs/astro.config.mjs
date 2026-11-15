// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

export default defineConfig({
  site: 'https://illesnagy1.github.io',
  base: '/ctfp',
  integrations: [
    starlight({
      title: 'CTF+',
      social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/illesnagy1/ctfp' }],
      sidebar: [{ label: 'Guides', items: [{ autogenerate: { directory: 'guides' } }] }],
    }),
  ],
});
