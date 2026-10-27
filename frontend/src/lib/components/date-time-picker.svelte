<script lang="ts">
  import {
    DateFormatter,
    getLocalTimeZone,
    parseAbsolute,
    Time,
    toCalendarDate,
    toCalendarDateTime,
    toZoned,
    type DateValue,
  } from '@internationalized/date';
  import ChevronDownIcon from '@lucide/svelte/icons/chevron-down';
  import type { ControlAttrs } from 'formsnap';

  import { Button } from '#lib/components/ui/button/index.js';
  import { Calendar } from '#lib/components/ui/calendar/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import * as Popover from '#lib/components/ui/popover/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import { getLocale } from '#lib/paraglide/runtime.js';

  let {
    value = $bindable(''),
    triggerProps = {},
  }: {
    /** An absolute ISO instant, e.g. `2026-10-27T09:05:00.000Z`; picked and shown in the browser's time zone. */
    value?: string;
    /** Spread onto the date button, e.g. formsnap's `Form.Control` props (id, aria-*), so the label points at it. */
    triggerProps?: Partial<ControlAttrs>;
  } = $props();

  let open = $state(false);

  // Only used once mounted (the picker sits in a dialog), so the browser's zone is the right one.
  const timeZone = getLocalTimeZone();

  const zoned = $derived(value ? parseAbsolute(value, timeZone) : undefined);
  const date = $derived(zoned && toCalendarDate(zoned));
  const time = $derived(zoned ? `${String(zoned.hour).padStart(2, '0')}:${String(zoned.minute).padStart(2, '0')}` : '');

  const dateFormat = $derived(new DateFormatter(getLocale(), { dateStyle: 'long' }));

  /** Combines a local date and `HH:mm` into the absolute instant; seconds are dropped. */
  function set(newDate: DateValue | undefined, newTime: string) {
    if (!newDate) return;
    const [hour = 0, minute = 0] = newTime.split(':').map(Number);
    value = toZoned(toCalendarDateTime(newDate, new Time(hour, minute)), timeZone).toAbsoluteString();
  }
</script>

<div class="flex gap-2">
  <Popover.Root bind:open>
    <Popover.Trigger>
      {#snippet child({ props })}
        <Button {...props} {...triggerProps} variant="outline" class="flex-1 justify-between">
          {date ? dateFormat.format(date.toDate(timeZone)) : m.date_time_picker_pick_date()}
          <ChevronDownIcon data-icon="inline-end" />
        </Button>
      {/snippet}
    </Popover.Trigger>
    <!-- The calendar brings its own padding and has to sit flush with the popover edges. -->
    <!-- eslint-disable-next-line shadcn/no-restyle -->
    <Popover.Content class="w-auto overflow-hidden p-0" align="start">
      <Calendar
        type="single"
        value={date}
        locale={getLocale()}
        captionLayout="dropdown"
        onValueChange={(picked) => {
          set(picked, time || '00:00');
          open = false;
        }}
      />
    </Popover.Content>
  </Popover.Root>
  <Input
    type="time"
    aria-label={m.date_time_picker_time()}
    disabled={!date}
    value={time}
    onchange={(e) => set(date, e.currentTarget.value)}
    class="w-28 appearance-none [&::-webkit-calendar-picker-indicator]:hidden [&::-webkit-calendar-picker-indicator]:appearance-none"
  />
</div>
