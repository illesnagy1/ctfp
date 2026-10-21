<script lang="ts">
  import { page } from '$app/state';
  import { onMount, type Snippet } from 'svelte';

  import { getLocale } from '#lib/paraglide/runtime.js';
  import { browserTimeZone } from '#lib/time.js';

  let {
    value,
    dateOnly = false,
    children,
  }: {
    /** ISO 8601 instant, as the api sends it. */
    value: string;
    dateOnly?: boolean;
    /** Wraps the formatted text, e.g. into a translated sentence. */
    children?: Snippet<[string]>;
  } = $props();

  // The server and hydration both use the zone from the cookie (UTC before it's set), so the markup matches;
  // once mounted, the browser's own zone takes over.
  let mountedZone = $state<string>();
  onMount(() => {
    mountedZone = browserTimeZone();
  });

  const timeZone = $derived(mountedZone ?? page.data.timeZone ?? 'UTC');
  // Without the cookie the server falls back to UTC; say so rather than pass it off as local time.
  const isFallback = $derived(!mountedZone && !page.data.timeZone);

  const text = $derived(
    dateOnly
      ? new Date(value).toLocaleDateString(getLocale(), { timeZone })
      : new Date(value).toLocaleString(getLocale(), { timeZone, timeZoneName: isFallback ? 'short' : undefined }),
  );
</script>

<time datetime={value}>
  {#if children}
    {@render children(text)}
  {:else}
    {text}
  {/if}
</time>
