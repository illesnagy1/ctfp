import { z } from 'zod';

import { m } from '#lib/paraglide/messages.js';

import { zRegisterRequest, zUserUpdateRequest } from './api.js';

// Limits come from the api's schemas (./api.ts); these only add what forms need on top: trimming, and empty for none.

/** Trims, then checks with [schema]: forms trim, the api checks what it's sent as is. */
export const trimmed = <T extends z.ZodType<unknown, string>>(schema: T) => z.string().trim().pipe(schema);

export const username = trimmed(zRegisterRequest.shape.username);

export const email = trimmed(zRegisterRequest.shape.email);

/** ISO 3166-1 alpha-2 code, e.g. HU, or empty for none. */
export const country = z
  .string()
  .trim()
  .toUpperCase()
  .pipe(zUserUpdateRequest.shape.country.unwrap().unwrap().or(z.literal('')));

/** An http(s) link, or empty for none; the profile only links http(s) URLs. */
export const website = z
  .string()
  .trim()
  // `example.com` means https://example.com; a link with another scheme (e.g. javascript:) is left to fail below.
  .transform((v) => (v && !/^[a-z][a-z\d+.-]*:/i.test(v) ? `https://${v}` : v))
  .pipe(
    zUserUpdateRequest.shape.website
      .unwrap()
      .unwrap()
      .regex(/^(https?:\/\/\S+)?$/i, { error: () => m.validation_website() }),
  );
