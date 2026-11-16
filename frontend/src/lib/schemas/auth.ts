import { z } from 'zod';

import { m } from '#lib/paraglide/messages.js';

import { email, newPassword, required, username } from './fields.js';

export const loginSchema = z.object({
  login: required(),
  password: required(),
});

const passwordsMatch = (data: { password: string; confirmPassword: string }) => data.password === data.confirmPassword;
const mismatch = { path: ['confirmPassword'], error: () => m.validation_passwords_match() };

export const registerSchema = z
  .object({
    username: username(),
    email: email(),
    password: newPassword(),
    confirmPassword: z.string(),
    registrationCode: z.string().trim(),
  })
  .refine(passwordsMatch, mismatch);

export const forgotPasswordSchema = z.object({ email: email() });

export const resetPasswordSchema = z
  .object({
    // From the emailed link, carried in a hidden field.
    token: required(),
    password: newPassword(),
    confirmPassword: z.string(),
  })
  .refine(passwordsMatch, mismatch);
