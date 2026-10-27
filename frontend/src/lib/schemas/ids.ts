import { z } from 'zod';

/** `id` as posted by `submitAction`. */
export const idSchema = z.object({ id: z.coerce.number().int() });

/** Comma-separated `ids`, as posted by `submitAction`. */
export const idsSchema = z.object({
  ids: z
    .string()
    .transform((ids) => ids.split(',').map(Number))
    .pipe(z.array(z.number().int()).min(1)),
});
