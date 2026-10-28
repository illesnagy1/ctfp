<script lang="ts">
  import { Time } from '@internationalized/date';
  import type { HTMLInputAttributes } from 'svelte/elements';

  import { Input } from '#lib/components/ui/input/index.js';

  let {
    value = $bindable(),
    disabled = false,
    class: className,
    ...restProps
  }: {
    value?: Time;
    disabled?: boolean;
    // The rest (e.g. aria-label, formsnap's `Form.Control` props) goes on the time input.
  } & Omit<HTMLInputAttributes, 'value' | 'type' | 'disabled' | 'files'> = $props();

  const text = $derived(value ? `${String(value.hour).padStart(2, '0')}:${String(value.minute).padStart(2, '0')}` : '');

  function onchange(e: Event & { currentTarget: HTMLInputElement }) {
    const [hour, minute] = e.currentTarget.value.split(':').map(Number);
    // Cleared or half-typed: keep the last complete time.
    if (hour !== undefined && minute !== undefined && !Number.isNaN(hour) && !Number.isNaN(minute)) {
      value = new Time(hour, minute);
    }
  }
</script>

<Input {...restProps} type="time" value={text} {disabled} {onchange} class={className} />
