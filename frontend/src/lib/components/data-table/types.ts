import type { Component } from 'svelte';

/** A value a column can be filtered by, as shown in a faceted filter or an option cell. */
export type DataTableOption = {
  label: string;
  value: string;
  icon?: Component;
};

/** Toolbar text search on one column. */
export type DataTableSearch = {
  column: string;
  placeholder: string;
};

/** Toolbar faceted filter on one column; the column needs a `filterFn` that matches any of the selected values. */
export type DataTableFilter = {
  column: string;
  title: string;
  options: DataTableOption[];
};
