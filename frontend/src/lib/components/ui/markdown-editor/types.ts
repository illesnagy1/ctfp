import type { AriaAttributes } from 'svelte/elements';

/** What a form field passes to its control (formsnap's props): id, name, aria-* state and its data-fs-* hooks. */
export type ControlAttributes = AriaAttributes & {
  id?: string;
  name?: string;
  [key: `data-${string}`]: string | undefined;
};
