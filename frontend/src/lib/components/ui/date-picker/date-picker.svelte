<script lang="ts">
  import type { DateValue } from '@internationalized/date';
  import CalendarIcon from '@lucide/svelte/icons/calendar';
  import { DatePicker } from 'bits-ui';

  import { buttonVariants } from '#lib/components/ui/button/index.js';
  import * as Calendar from '#lib/components/ui/calendar/index.js';
  import { cn, type WithoutChildrenOrChild } from '#lib/utils.js';

  let {
    value = $bindable(),
    locale,
    triggerLabel,
    class: className,
    ...restProps
  }: {
    value?: DateValue;
    /** Order and format of the segments and the calendar, e.g. the page's language. */
    locale: string;
    /** Accessible name of the calendar button. */
    triggerLabel: string;
    // The rest (e.g. formsnap's `Form.Control` props: id, aria-*) goes on the segment group.
  } & WithoutChildrenOrChild<DatePicker.InputProps> = $props();

  // The month the calendar shows; follows the value, and the month/year dropdowns move it.
  let placeholder = $state<DateValue>();
</script>

<!-- A segmented date field (year, month, day in the locale's order) with a calendar button. -->
<DatePicker.Root bind:value bind:placeholder {locale} granularity="day" preventDeselect closeOnDateSelect>
  <DatePicker.Input
    class={cn(
      'flex h-9 w-full min-w-0 items-center gap-1 rounded-md border border-input bg-transparent ps-2.5 pe-1 text-base shadow-xs transition-[color,box-shadow] focus-within:border-ring focus-within:ring-3 focus-within:ring-ring/50 aria-invalid:border-destructive aria-invalid:ring-3 aria-invalid:ring-destructive/20 md:text-sm dark:bg-input/30 dark:aria-invalid:border-destructive/50 dark:aria-invalid:ring-destructive/40',
      className,
    )}
    {...restProps}
  >
    {#snippet children({ segments })}
      <div class="flex flex-1 items-center">
        {#each segments as { part, value: text }, i (i)}
          <DatePicker.Segment
            {part}
            class="rounded-sm px-0.5 tabular-nums outline-none focus:bg-accent focus:text-accent-foreground data-placeholder:text-muted-foreground data-[segment=literal]:px-0 data-[segment=literal]:text-muted-foreground"
          >
            {text}
          </DatePicker.Segment>
        {/each}
      </div>
      <DatePicker.Trigger
        aria-label={triggerLabel}
        class={cn(buttonVariants({ variant: 'ghost', size: 'icon-sm' }), 'size-7')}
      >
        <CalendarIcon />
      </DatePicker.Trigger>
    {/snippet}
  </DatePicker.Input>
  <DatePicker.Portal>
    <DatePicker.Content
      align="end"
      sideOffset={6}
      class="z-50 origin-(--bits-popover-content-transform-origin) overflow-hidden rounded-md bg-popover text-popover-foreground shadow-md ring-1 ring-foreground/10 outline-hidden duration-100 data-[side=bottom]:slide-in-from-top-2 data-[side=top]:slide-in-from-bottom-2 data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95"
    >
      <!-- The shadcn calendar's parts, on the picker's own calendar state. -->
      <DatePicker.Calendar class="group/calendar p-3 [--cell-radius:var(--radius-md)] [--cell-size:--spacing(8)]">
        {#snippet children({ months, weekdays })}
          <Calendar.Months>
            <Calendar.Nav>
              <Calendar.PrevButton variant="ghost" />
              <Calendar.NextButton variant="ghost" />
            </Calendar.Nav>
            {#each months as month, monthIndex (month)}
              <Calendar.Month>
                <Calendar.Header>
                  <Calendar.Caption
                    captionLayout="dropdown"
                    months={undefined}
                    monthFormat="short"
                    years={undefined}
                    yearFormat="numeric"
                    month={month.value}
                    bind:placeholder
                    {locale}
                    {monthIndex}
                  />
                </Calendar.Header>
                <Calendar.Grid>
                  <Calendar.GridHead>
                    <Calendar.GridRow class="select-none">
                      {#each weekdays as weekday, i (i)}
                        <Calendar.HeadCell>{weekday.slice(0, 2)}</Calendar.HeadCell>
                      {/each}
                    </Calendar.GridRow>
                  </Calendar.GridHead>
                  <Calendar.GridBody>
                    {#each month.weeks as weekDates (weekDates)}
                      <Calendar.GridRow class="mt-2 w-full">
                        {#each weekDates as date (date)}
                          <Calendar.Cell {date} month={month.value}>
                            <Calendar.Day />
                          </Calendar.Cell>
                        {/each}
                      </Calendar.GridRow>
                    {/each}
                  </Calendar.GridBody>
                </Calendar.Grid>
              </Calendar.Month>
            {/each}
          </Calendar.Months>
        {/snippet}
      </DatePicker.Calendar>
    </DatePicker.Content>
  </DatePicker.Portal>
</DatePicker.Root>
