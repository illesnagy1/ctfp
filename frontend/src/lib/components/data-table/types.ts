import type { CellData, RowData, TableFeatures } from '@tanstack/svelte-table';
import type { Component } from 'svelte';

/**
 * A label. Pass a Paraglide message function (`m.users_column_email`) instead of calling it where the value is created
 * once, e.g. in a module-level column list: it's then resolved on every render, in the current locale.
 */
export type Text = string | (() => string);

export const resolveText = (text: Text) => (typeof text === 'function' ? text() : text);

declare module '@tanstack/svelte-table' {
  interface ColumnMeta<in out TFeatures extends TableFeatures, in out TData extends RowData, TValue extends CellData> {
    /** Shown in the column header and the view options. Falls back to the column id. */
    title?: Text;
  }
}

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
