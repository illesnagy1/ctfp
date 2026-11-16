<script lang="ts">
  import CheckIcon from '@lucide/svelte/icons/check';
  import ChevronsUpDownIcon from '@lucide/svelte/icons/chevrons-up-down';
  import { countries } from 'country-flag-icons';
  import type { ControlAttrs } from 'formsnap';
  import { tick } from 'svelte';

  import CountryFlag from '#lib/components/country-flag.svelte';
  import { Button } from '#lib/components/ui/button/index.js';
  import * as Command from '#lib/components/ui/command/index.js';
  import * as Popover from '#lib/components/ui/popover/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { getLocale } from '#lib/paraglide/runtime.js';
  import { cn } from '#lib/utils.js';

  /** A searchable country picker; `value` is an ISO 3166-1 alpha-2 code, or '' for none. `Form.Control`'s props go on the trigger. */
  let { value = $bindable(''), ...triggerProps }: { value?: string } & Partial<ControlAttrs> = $props();

  let open = $state(false);
  let triggerRef = $state<HTMLButtonElement | null>(null);

  // Regions only; the flag set also has subdivisions such as GB-SCT, which the api's two-letter codes can't hold.
  const options = $derived.by(() => {
    const names = new Intl.DisplayNames([getLocale()], { type: 'region' });
    return countries
      .filter((code) => /^[A-Z]{2}$/.test(code))
      .map((code) => ({ code, name: names.of(code) ?? code }))
      .toSorted((a, b) => a.name.localeCompare(b.name, getLocale()));
  });
  const selected = $derived(options.find((o) => o.code === value?.toUpperCase()));

  // Back to the trigger, so keyboard users carry on through the form.
  function pick(code: string) {
    value = code;
    open = false;
    void tick().then(() => triggerRef?.focus());
  }
</script>

<Popover.Root bind:open>
  <Popover.Trigger bind:ref={triggerRef}>
    {#snippet child({ props })}
      <Button
        {...triggerProps}
        {...props}
        variant="outline"
        role="combobox"
        aria-expanded={open}
        class="w-full justify-between"
      >
        {#if selected}
          <span class="flex items-center gap-2"><CountryFlag code={selected.code} />{selected.name}</span>
        {:else}
          <span class="text-muted-foreground">{m.users_field_country_none()}</span>
        {/if}
        <ChevronsUpDownIcon class="opacity-50" />
      </Button>
    {/snippet}
  </Popover.Trigger>
  <!-- The command list brings its own padding and has to sit flush with the popover edges. -->
  <!-- eslint-disable-next-line shadcn/no-restyle -->
  <Popover.Content class="w-(--bits-popover-anchor-width) p-0" align="start">
    <Command.Root>
      <Command.Input placeholder={m.users_column_country()} />
      <Command.List>
        <Command.Empty>{m.data_table_filter_no_results()}</Command.Empty>
        <Command.Group>
          <Command.Item value={m.users_field_country_none()} onSelect={() => pick('')}>
            <CheckIcon class={cn(selected && 'text-transparent')} />
            {m.users_field_country_none()}
          </Command.Item>
          {#each options as option (option.code)}
            <Command.Item value={option.name} keywords={[option.code]} onSelect={() => pick(option.code)}>
              <CheckIcon class={cn(selected?.code !== option.code && 'text-transparent')} />
              <CountryFlag code={option.code} />
              {option.name}
            </Command.Item>
          {/each}
        </Command.Group>
      </Command.List>
    </Command.Root>
  </Popover.Content>
</Popover.Root>
