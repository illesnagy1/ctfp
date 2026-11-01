import { compareItems, rankItem, type RankingInfo } from '@tanstack/match-sorter-utils';
import type { FilterFn, Row, RowData, SortFn, TableFeatures } from '@tanstack/svelte-table';

export interface FuzzyFilterMeta {
  itemRank?: RankingInfo;
}

type FuzzyFeatures = TableFeatures & { filterMeta: FuzzyFilterMeta };

// The global filter runs per column and stops at the first match, so it would only rank that column. Rank the whole
// row instead, once per search: the best match of any searchable column. Rows are rebuilt when the data changes.
const rowRanks = new WeakMap<object, { query: string; rank: RankingInfo }>();

function rankRow(row: Row<FuzzyFeatures, RowData>, query: string): RankingInfo {
  const cached = rowRanks.get(row);
  if (cached?.query === query) return cached.rank;

  const values = row
    .getAllCells()
    .filter((cell) => cell.column.getCanGlobalFilter())
    .flatMap((cell) => {
      const value = cell.getValue();
      // match-sorter would turn a missing value into the text "null".
      return value == null ? [] : [value].flat().map(String);
    });
  // Matches case-insensitively, ignoring accents ("szabo" finds "Szabó"), and down to the query's letters appearing
  // in order ("jdoe" finds "john.doe").
  const rank = rankItem(values, query, { accessors: [(items) => items] });
  rowRanks.set(row, { query, rank });
  return rank;
}

/** Global filter: keeps the rows that match the search anywhere, and records how well for `fuzzySort`. */
export const fuzzyFilter: FilterFn<FuzzyFeatures, RowData> = Object.assign(
  (row: Row<FuzzyFeatures, RowData>, _columnId: string, query: string, addMeta?: (meta: FuzzyFilterMeta) => void) => {
    const itemRank = rankRow(row, query);
    addMeta?.({ itemRank });
    return itemRank.passed;
  },
  { resolveFilterValue: (value: unknown) => String(value).trim() },
);

/** Best matches first (exact, then starts with, contains, ...); ties keep the data's order. */
export const fuzzySort: SortFn<FuzzyFeatures, RowData> = (rowA, rowB) => {
  const rankA = Object.values(rowA.columnFiltersMeta).find((meta) => meta?.itemRank)?.itemRank;
  const rankB = Object.values(rowB.columnFiltersMeta).find((meta) => meta?.itemRank)?.itemRank;
  return rankA && rankB ? compareItems(rankA, rankB) : 0;
};
