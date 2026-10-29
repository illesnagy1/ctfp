<script lang="ts" module>
  // Each flag is its own asset, fetched only where it's shown; `no-inline` keeps them out of the bundle as data URIs.
  const files: Record<string, string> = import.meta.glob('/node_modules/country-flag-icons/3x2/*.svg', {
    query: '?url&no-inline',
    import: 'default',
    eager: true,
  });
  const flags = new Map(
    Object.entries(files).map(([path, url]) => [path.slice(path.lastIndexOf('/') + 1, -'.svg'.length), url]),
  );
</script>

<script lang="ts">
  import { getLocale } from '#lib/paraglide/runtime.js';

  /** ISO 3166-1 alpha-2 code, as the api stores it; the code itself is shown when there's no flag for it. */
  let { code }: { code: string | null | undefined } = $props();

  const upper = $derived(code?.toUpperCase());
  const flag = $derived(upper && flags.get(upper));
  const name = $derived.by(() => {
    if (!upper) return undefined;
    try {
      return new Intl.DisplayNames([getLocale()], { type: 'region' }).of(upper);
    } catch {
      // Not a well-formed region code.
      return undefined;
    }
  });
</script>

{#if flag}
  <img src={flag} alt={name ?? upper} title={name ?? upper} class="inline-block h-4 w-6 rounded-xs" />
{:else if upper}
  <span title={name}>{upper}</span>
{:else}
  -
{/if}
