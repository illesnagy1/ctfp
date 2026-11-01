import { z } from 'zod';

import { m } from '#lib/paraglide/messages.js';

// Messages are functions, so they're translated when a field fails, in the current request's locale, not when this
// module loads. Limits mirror the api's validation (api/.../UserValidation.kt), which has the final say.

export const username = () =>
  z
    .string()
    .trim()
    .regex(/^[A-Za-z0-9._-]{1,16}$/, { error: () => m.validation_username() });

export const email = () =>
  z
    .string()
    .trim()
    .max(255, { error: () => m.validation_email() })
    .pipe(z.email({ error: () => m.validation_email() }));

export const newPassword = () =>
  z
    .string()
    .min(8, { error: () => m.validation_password_length() })
    .max(128, { error: () => m.validation_password_length() });

/** ISO 3166-1 alpha-2 code, e.g. HU, or empty for none. */
export const country = () =>
  z
    .string()
    .trim()
    .toUpperCase()
    .regex(/^([A-Z]{2})?$/, { error: () => m.validation_country() });

/** An http(s) link, or empty for none; the profile only links http(s) URLs. */
export const website = () =>
  z
    .string()
    .trim()
    .max(255, { error: () => m.validation_too_long({ max: 255 }) })
    .regex(/^(https?:\/\/\S+)?$/i, { error: () => m.validation_website() });

export const required = () => z.string().min(1, { error: () => m.validation_required() });

/** A limit where 0 means none, e.g. max attempts. */
export const count = () =>
  z
    .number({ error: () => m.validation_whole_number() })
    .int({ error: () => m.validation_whole_number() })
    .min(0, { error: () => m.validation_whole_number() });
