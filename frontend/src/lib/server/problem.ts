import type { ProblemDetails } from '#lib/api/index.js';

// The api answers errors with RFC 9457 problem details (`application/problem+json`, ProblemDetails in #lib/api).
// These read them defensively: an error body could also come from something in between, e.g. a proxy's HTML page.

const isObject = (value: unknown): value is Record<string, unknown> => typeof value === 'object' && value !== null;

/** What went wrong, for people: the problem's `detail`, else its `title` (e.g. "Not Found"). */
export function problemMessage(problem: ProblemDetails): string;
export function problemMessage(problem: unknown): string | undefined;
export function problemMessage(problem: unknown): string | undefined {
  if (!isObject(problem)) return undefined;
  if (typeof problem.detail === 'string') return problem.detail;
  return typeof problem.title === 'string' ? problem.title : undefined;
}

/** `#/items/1/name` → `['items', '1', 'name']`, undoing RFC 6901's escapes. */
export const pointerPath = (pointer: string) =>
  pointer
    .replace(/^#?\//, '')
    .split('/')
    .map((key) => key.replaceAll('~1', '/').replaceAll('~0', '~'));

/** The body fields a 400 from request validation names, as paths into the body, with what's wrong with each. */
export function problemFields(problem: unknown): { path: string[]; detail: string }[] {
  if (!isObject(problem) || !Array.isArray(problem.errors)) return [];
  return problem.errors.flatMap((error: unknown) =>
    isObject(error) && typeof error.pointer === 'string' && typeof error.detail === 'string'
      ? [{ path: pointerPath(error.pointer), detail: error.detail }]
      : [],
  );
}
