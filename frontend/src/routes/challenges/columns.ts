import { resolve } from '$app/paths';
import { renderComponent, type ColumnDef } from '@tanstack/svelte-table';

import type { ChallengeResponse } from '#lib/api/index.js';
import {
  DataTableBadgesCell,
  DataTableLinkCell,
  selectColumn,
  sortableColumn,
  type DataTableFeatures,
} from '#lib/components/data-table/index.js';
import LocalTime from '#lib/components/local-time.svelte';
import { m } from '#lib/paraglide/messages.js';

import RowActions from './row-actions.svelte';

// Titles are message functions, not strings, so they're translated on each render (see `Text`).
export const columns: ColumnDef<DataTableFeatures, ChallengeResponse>[] = [
  {
    ...sortableColumn('title', m.challenges_column_title),
    cell: ({ row }) =>
      renderComponent(DataTableLinkCell, {
        href: resolve('/challenges/[id]', { id: String(row.original.id) }),
        text: row.original.title,
      }),
  },
  {
    ...sortableColumn('categories', m.challenges_column_categories),
    enableSorting: false,
    cell: ({ row }) => renderComponent(DataTableBadgesCell, { badges: row.original.categories }),
    // One challenge can have several categories: facet on each, and match if any is selected.
    getUniqueValues: (challenge) => challenge.categories,
    filterFn: (row, _id, selected: string[]) => row.original.categories.some((c) => selected.includes(c)),
  },
  { ...sortableColumn('value', m.challenges_column_points), enableGlobalFilter: false },
  { ...sortableColumn('solves', m.challenges_column_solves), enableGlobalFilter: false },
  {
    ...sortableColumn('maxAttempts', m.challenges_column_max_attempts),
    cell: ({ row }) => row.original.maxAttempts || m.challenge_details_no_limit(),
  },
];

/** With row selection for bulk actions, and the row menu (delete); players have no row actions. */
export const adminColumns: ColumnDef<DataTableFeatures, ChallengeResponse>[] = [
  selectColumn<ChallengeResponse>(),
  ...columns,
  // Players only ever see published challenges, so the date is admin-only.
  {
    ...sortableColumn('publishAt', m.challenges_column_publish_at),
    // The raw value is an ISO instant, not the local time shown.
    enableGlobalFilter: false,
    cell: ({ row }) => renderComponent(LocalTime, { value: row.original.publishAt }),
  },
  {
    id: 'actions',
    enableSorting: false,
    enableHiding: false,
    cell: ({ row }) => renderComponent(RowActions, { id: row.original.id, title: row.original.title }),
  },
];
