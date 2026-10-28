<script lang="ts">
  import {
    getLocalTimeZone,
    parseAbsolute,
    Time,
    toCalendarDate,
    toCalendarDateTime,
    toZoned,
    type DateValue,
  } from '@internationalized/date';
  import type { ComponentProps } from 'svelte';

  import { DatePicker } from '#lib/components/ui/date-picker/index.js';
  import { TimePicker } from '#lib/components/ui/time-picker/index.js';

  let {
    value = $bindable(''),
    locale,
    triggerLabel,
    timeLabel,
    ...restProps
  }: {
    /** An absolute ISO instant, e.g. `2026-10-27T09:05:00.000Z`; edited in the browser's time zone. */
    value?: string;
    locale: string;
    /** Accessible name of the calendar button. */
    triggerLabel: string;
    /** Accessible name of the time field. */
    timeLabel: string;
    // The rest (e.g. formsnap's `Form.Control` props) goes on the date field, so the field's label points at it.
  } & Omit<ComponentProps<typeof DatePicker>, 'value' | 'locale' | 'triggerLabel'> = $props();

  // Only used once mounted (the picker sits in a dialog), so the browser's zone is the right one.
  const timeZone = getLocalTimeZone();

  const zoned = $derived(value ? parseAbsolute(value, timeZone) : undefined);
  const date = $derived(zoned && toCalendarDate(zoned));
  const time = $derived(zoned && new Time(zoned.hour, zoned.minute));

  /** Combines the local date and time into the absolute instant; a new date without a time yet starts at midnight. */
  function set(newDate: DateValue | undefined, newTime: Time | undefined) {
    if (!newDate) return;
    value = toZoned(toCalendarDateTime(newDate, newTime ?? new Time(0, 0)), timeZone).toAbsoluteString();
  }
</script>

<div class="flex gap-2">
  <DatePicker
    {...restProps}
    bind:value={() => date, (picked) => set(picked, time)}
    {locale}
    {triggerLabel}
    class="flex-1"
  />
  <TimePicker
    bind:value={() => time, (picked) => set(date, picked)}
    disabled={!date}
    aria-label={timeLabel}
    class="w-32"
  />
</div>
