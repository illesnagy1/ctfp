<script lang="ts">
  import { resolve } from '$app/paths';
  import { page } from '$app/state';
  import { ChevronDown, CircleUser, Flag, LogIn, LogOut, Menu, UserPlus, Wrench, X } from '@lucide/svelte';
  import { ModeWatcher } from 'mode-watcher';

  import './layout.css';
  import favicon from '#lib/assets/favicon.svg';
  import { Button } from '#lib/components/ui/button/index.js';
  import { ConfirmDeleteDialog } from '#lib/components/ui/confirm-delete-dialog/index.js';
  import * as DropdownMenu from '#lib/components/ui/dropdown-menu/index.js';
  import { LanguageSwitcher, type Language } from '#lib/components/ui/language-switcher/index.js';
  import { Toaster } from '#lib/components/ui/sonner/index.js';
  import { ThemeSelector } from '#lib/components/ui/theme-selector/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { getLocale, isLocale, setLocale } from '#lib/paraglide/runtime.js';
  import { setShadcnThemeContext } from '#lib/sjsf/shadcn.js';
  import { TIME_ZONE_COOKIE, browserTimeZone } from '#lib/time.js';

  let { children, data } = $props();
  let menuOpen = $state(false);
  let isAdmin = $derived(data.user?.role === 'ADMIN');
  let logoutForm = $state<HTMLFormElement>();

  // The main task first.
  const navLinks = [
    { href: resolve('/challenges'), label: m.nav_challenges },
    { href: resolve('/scoreboard'), label: m.nav_scoreboard },
    { href: resolve('/users'), label: m.nav_users },
  ];
  const isCurrent = (href: string) => page.url.pathname === href || page.url.pathname.startsWith(`${href}/`);

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

  setShadcnThemeContext();
</script>

<svelte:head><link rel="icon" href={favicon} /></svelte:head>

<!-- Its no-flash theme script is in app.html, where it gets the CSP nonce (hooks.server.ts fills it in). -->
<ModeWatcher disableHeadScriptInjection />
<!-- Shared by every `confirmDelete()` call. -->
<ConfirmDeleteDialog />
<!-- Shows `toast.success()` / `toast.error()` from form actions. -->
<Toaster richColors closeButton />

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
        <!-- Teams, Team and Notifications stay out of the nav until their pages exist. -->
        <div class="flex flex-col gap-2 md:flex-row md:items-center">
          {#each navLinks as link (link.href)}
            <Button
              href={link.href}
              variant={isCurrent(link.href) ? 'secondary' : 'ghost'}
              aria-current={isCurrent(link.href) ? 'page' : undefined}
            >
              {link.label()}
            </Button>
          {/each}
        </div>
        <div class="mt-3 flex flex-col gap-2 md:mt-0 md:flex-row md:items-center">
          <div class="flex items-center gap-2">
            <LanguageSwitcher
              {languages}
              variant="ghost"
              value={getLocale()}
              onChange={(code) => {
                if (isLocale(code)) setLocale(code);
              }}
            />
            <ThemeSelector variant="ghost" />
          </div>
          {#if data.user}
            <form bind:this={logoutForm} method="post" action={resolve('/(auth)/logout')} class="hidden"></form>
            <DropdownMenu.Root>
              <DropdownMenu.Trigger>
                {#snippet child({ props })}
                  <Button {...props} variant="ghost" class="justify-start">
                    <CircleUser />{data.user?.username}<ChevronDown class="text-muted-foreground" />
                  </Button>
                {/snippet}
              </DropdownMenu.Trigger>
              <DropdownMenu.Content align="end" class="w-48">
                <DropdownMenu.Item>
                  {#snippet child({ props })}
                    <a {...props} href={resolve('/users/[id]', { id: String(data.user?.id) })}>
                      <CircleUser />{m.nav_profile()}
                    </a>
                  {/snippet}
                </DropdownMenu.Item>
                {#if isAdmin}
                  <DropdownMenu.Item>
                    {#snippet child({ props })}
                      <a {...props} href={resolve('/admin')}><Wrench />{m.nav_admin()}</a>
                    {/snippet}
                  </DropdownMenu.Item>
                  <DropdownMenu.Item>
                    {#snippet child({ props })}
                      <a {...props} href={resolve('/admin/submissions')}><Flag />{m.nav_submissions()}</a>
                    {/snippet}
                  </DropdownMenu.Item>
                {/if}
                <DropdownMenu.Separator />
                <DropdownMenu.Item onSelect={() => logoutForm?.requestSubmit()}>
                  <LogOut />{m.nav_logout()}
                </DropdownMenu.Item>
              </DropdownMenu.Content>
            </DropdownMenu.Root>
          {:else}
            <Button href={resolve('/(auth)/register')}><UserPlus />{m.nav_register()}</Button>
            <Button href={resolve('/(auth)/login')} variant="outline"><LogIn />{m.nav_login()}</Button>
          {/if}
        </div>
      </div>
    </nav>
  </header>

  <main class="grow">
    {@render children?.()}
  </main>

  <footer class="border-t bg-background/95">
    <div class="mx-auto flex max-w-6xl items-center justify-center p-4">
      <a href={resolve('/')} class="text-xs text-muted-foreground hover:underline">CTF+</a>
    </div>
  </footer>
</div>
