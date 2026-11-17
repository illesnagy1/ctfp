import { resolve } from '$app/paths';
import { renderComponent, type ColumnDef } from '@tanstack/svelte-table';

import {
  DataTableBadgeCell,
  DataTableBadgesCell,
  DataTableColumnHeader,
  DataTableLinkCell,
  includesValue,
  sortableColumn,
  type DataTableFeatures,
  type DataTableOption,
} from '#lib/components/data-table/index.js';
import LocalTime from '#lib/components/local-time.svelte';
import { m } from '#lib/paraglide/messages.js';
import type { components } from '#lib/types/api.js';

export type Submission = components['schemas']['SubmissionResponse'];

type SubmissionResult = 'correct' | 'wrong';

const result = (submission: Submission): SubmissionResult => (submission.isCorrect ? 'correct' : 'wrong');

/** Result options in the current locale; call it where it renders, not at module level. */
export const resultOptions = (): DataTableOption[] => [
  { value: 'correct', label: m.submissions_result_correct() },
  { value: 'wrong', label: m.submissions_result_wrong() },
];

const resultLabel = (value: SubmissionResult) => resultOptions().find((o) => o.value === value)?.label ?? value;

/** A player's own guesses: what, when, and from where. */
export const columns: ColumnDef<DataTableFeatures, Submission>[] = [
  {
    ...sortableColumn('submittedAt', m.submissions_column_time),
    // The raw value is an ISO instant, not the local time shown.
    enableGlobalFilter: false,
    cell: ({ row }) => renderComponent(LocalTime, { value: row.original.submittedAt }),
  },
  {
    ...sortableColumn('challengeTitle', m.submissions_column_challenge),
    cell: ({ row }) =>
      renderComponent(DataTableLinkCell, {
        href: resolve('/challenges/[id]', { id: String(row.original.challengeId) }),
        text: row.original.challengeTitle,
      }),
  },
  {
    ...sortableColumn('submittedFlag', m.submissions_column_flag),
    cell: ({ row }) => renderComponent(DataTableBadgeCell, { value: row.original.submittedFlag }),
  },
  {
    id: 'result',
    meta: { title: m.submissions_column_result },
    header: ({ column }) =>
      renderComponent(DataTableColumnHeader<Submission, unknown>, { title: m.submissions_column_result(), column }),
    accessorFn: result,
    cell: ({ row }) => renderComponent(DataTableBadgesCell, { badges: [resultLabel(result(row.original))] }),
    filterFn: includesValue,
    // The search would see the raw key, not the translated label; the faceted filter covers it.
    enableGlobalFilter: false,
  },
  {
    ...sortableColumn('ipAddress', m.submissions_column_ip),
    cell: ({ row }) => row.original.ipAddress ?? '—',
  },
  {
    ...sortableColumn('userAgent', m.submissions_column_user_agent),
    cell: ({ row }) => renderComponent(DataTableBadgeCell, { value: row.original.userAgent ?? '—' }),
  },
];

/** Everyone's guesses for admins: the player too, linking to their profile. */
export const adminColumns: ColumnDef<DataTableFeatures, Submission>[] = [
  columns[0],
  {
    ...sortableColumn('username', m.submissions_column_user),
    cell: ({ row }) =>
      renderComponent(DataTableLinkCell, {
        href: resolve('/users/[id]', { id: String(row.original.userId) }),
        text: row.original.username,
      }),
  },
  ...columns.slice(1),
];
