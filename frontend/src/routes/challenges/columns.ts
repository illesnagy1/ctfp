import { renderComponent, type ColumnDef } from '@tanstack/svelte-table';

import { sortableColumn, type DataTableFeatures } from '#lib/components/data-table/index.js';
import { m } from '#lib/paraglide/messages.js';
import { getLocale } from '#lib/paraglide/runtime.js';
import type { components } from '#lib/types/api.js';

import CategoriesCell from './categories-cell.svelte';
import RowActions from './row-actions.svelte';

export type Challenge = components['schemas']['ChallengeResponse'];

// Titles are message functions, not strings, so they're translated on each render (see `Text`).
export const columns: ColumnDef<DataTableFeatures, Challenge>[] = [
  sortableColumn('title', m.challenges_column_title),
  {
    ...sortableColumn('categories', m.challenges_column_categories),
    enableSorting: false,
    cell: ({ row }) => renderComponent(CategoriesCell, { categories: row.original.categories }),
    // One challenge can have several categories: facet on each, and match if any is selected.
    getUniqueValues: (challenge) => challenge.categories,
    filterFn: (row, _id, selected: string[]) => row.original.categories.some((c) => selected.includes(c)),
  },
  sortableColumn('maxAttempts', m.challenges_column_max_attempts),
  {
    ...sortableColumn('publishAt', m.challenges_column_publish_at),
    cell: ({ row }) => new Date(row.original.publishAt).toLocaleString(getLocale()),
  },
  {
    id: 'actions',
    enableSorting: false,
    enableHiding: false,
    cell: ({ row }) => renderComponent(RowActions, { id: row.original.id }),
  },
];
