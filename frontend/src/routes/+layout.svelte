<script lang="ts">
  import { resolve } from '$app/paths';

  import './layout.css';
  import { Bell, CircleUser, LogIn, LogOut, Menu, UserPlus, Users, Wrench, X } from '@lucide/svelte';

  import favicon from '#lib/assets/favicon.svg';
  import { Button } from '#lib/components/ui/button/index.js';
  import { LanguageSwitcher, type Language } from '#lib/components/ui/language-switcher/index.js';
  import { ThemeSelector } from '#lib/components/ui/theme-selector/index.js';

  let { children, data } = $props();
  let menuOpen = $state(false);
  let loggedIn = $derived(!!data.user);
  let isAdmin = $derived(data.user?.role === 'ADMIN');

  const languages: Language[] = [
    { code: 'en', label: 'English' },
    { code: 'hu', label: 'Magyar' },
  ];
</script>

<svelte:head><link rel="icon" href={favicon} /></svelte:head>

<div class="flex min-h-screen flex-col">
  <header class="sticky top-0 z-50 border-b bg-background/95 backdrop-blur">
    <nav class="mx-auto flex max-w-6xl flex-wrap items-center gap-3 px-4 py-3" aria-label="Primary">
      <Button href={resolve('/')} variant="ghost" size="lg">CTF+</Button>
      <div class="ml-auto md:hidden">
        <Button
          variant="ghost"
          size="icon"
          type="button"
          aria-controls="site-nav-links"
          aria-expanded={menuOpen}
          onclick={() => (menuOpen = !menuOpen)}
        >
          <span class="sr-only">Toggle navigation</span>
          {#if menuOpen}
            <X />
          {:else}
            <Menu />
          {/if}
        </Button>
      </div>
      <div
        id="site-nav-links"
        class:hidden={!menuOpen}
        class="w-full md:flex md:w-auto md:flex-1 md:items-center md:justify-between"
      >
        <div class="flex flex-col gap-2 md:flex-row md:items-center">
          <Button href={resolve('users')} variant="ghost">Users</Button>
          <Button href={resolve('teams')} variant="ghost">Teams</Button>
          <Button href={resolve('scoreboard')} variant="ghost">Scoreboard</Button>
          <Button href={resolve('challenges')} variant="ghost">Challenges</Button>
        </div>
        <div class="mt-3 flex flex-col gap-2 md:mt-0 md:flex-row md:items-center">
          {#if loggedIn}
            {#if isAdmin}
              <Button href={resolve('admin')} variant="ghost"><Wrench />Admin panel</Button>
            {/if}

            <Button href={resolve('notifications')} variant="ghost"><Bell />Notifications</Button>
            <Button href={resolve('team')} variant="ghost"><Users />Team</Button>

            <Button href={resolve('/users/[id]', { id: String(data.user?.id) })} variant="ghost">
              <CircleUser />Profile
            </Button>

            <form method="post" action={resolve('logout')}>
              <Button type="submit" variant="outline" class="w-full"><LogOut />Logout</Button>
            </form>
          {:else}
            <Button href={resolve('register')}><UserPlus />Register</Button>
            <Button href={resolve('login')} variant="outline"><LogIn />Login</Button>
          {/if}
          <LanguageSwitcher {languages} />
          <ThemeSelector />
        </div>
      </div>
    </nav>
  </header>

  <main class="grow text-justify">
    {@render children?.()}
  </main>

  <footer class="border-t bg-background/95">
    <div class="mx-auto flex max-w-6xl items-center justify-center px-4 py-6">
      <a href="/" rel="noreferrer" target="_blank" class="text-xs text-muted-foreground hover:underline">CTF+</a>
    </div>
  </footer>
</div>
