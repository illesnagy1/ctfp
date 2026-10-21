<script lang="ts">
  import { resolve } from '$app/paths';

  import './layout.css';
  import { Bell, CircleUser, LogIn, LogOut, Menu, UserPlus, Users, Wrench, X } from '@lucide/svelte';
  import { ModeWatcher } from 'mode-watcher';

  import favicon from '#lib/assets/favicon.svg';
  import { Button } from '#lib/components/ui/button/index.js';
  import { LanguageSwitcher, type Language } from '#lib/components/ui/language-switcher/index.js';
  import { ThemeSelector } from '#lib/components/ui/theme-selector/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { getLocale, isLocale, setLocale } from '#lib/paraglide/runtime.js';
  import { TIME_ZONE_COOKIE, browserTimeZone } from '#lib/time.js';

  let { children, data } = $props();
  let menuOpen = $state(false);
  let loggedIn = $derived(!!data.user);
  let isAdmin = $derived(data.user?.role === 'ADMIN');

  // Lets the server render times in the browser's zone from the next request on (see LocalTime).
  $effect(() => {
    const timeZone = browserTimeZone();
    if (timeZone !== data.timeZone) {
      document.cookie = `${TIME_ZONE_COOKIE}=${encodeURIComponent(timeZone)}; path=/; max-age=31536000; samesite=lax`;
    }
  });

  const languages: Language[] = [
    { code: 'en', label: 'English' },
    { code: 'hu', label: 'Magyar' },
  ];
</script>

<svelte:head><link rel="icon" href={favicon} /></svelte:head>

<ModeWatcher />

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
          <span class="sr-only">{m.nav_toggle()}</span>
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
          <Button href={resolve('users')} variant="ghost">{m.nav_users()}</Button>
          <Button href={resolve('teams')} variant="ghost">{m.nav_teams()}</Button>
          <Button href={resolve('scoreboard')} variant="ghost">{m.nav_scoreboard()}</Button>
          <Button href={resolve('challenges')} variant="ghost">{m.nav_challenges()}</Button>
        </div>
        <div class="mt-3 flex flex-col gap-2 md:mt-0 md:flex-row md:items-center">
          {#if loggedIn}
            {#if isAdmin}
              <Button href={resolve('admin')} variant="ghost"><Wrench />{m.nav_admin()}</Button>
            {/if}

            <Button href={resolve('notifications')} variant="ghost"><Bell />{m.nav_notifications()}</Button>
            <Button href={resolve('team')} variant="ghost"><Users />{m.nav_team()}</Button>

            <Button href={resolve('/users/[id]', { id: String(data.user?.id) })} variant="ghost">
              <CircleUser />{m.nav_profile()}
            </Button>

            <form method="post" action={resolve('logout')}>
              <Button type="submit" variant="outline" class="w-full"><LogOut />{m.nav_logout()}</Button>
            </form>
          {:else}
            <Button href={resolve('register')}><UserPlus />{m.nav_register()}</Button>
            <Button href={resolve('login')} variant="outline"><LogIn />{m.nav_login()}</Button>
          {/if}
          <!-- setLocale stores the choice in the PARAGLIDE_LOCALE cookie and reloads, so the server renders it too. -->
          <LanguageSwitcher
            {languages}
            value={getLocale()}
            onChange={(code) => {
              if (isLocale(code)) setLocale(code);
            }}
          />
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
