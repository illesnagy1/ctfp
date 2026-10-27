import { z } from 'zod';

import { m } from '#lib/paraglide/messages.js';

import { email, newPassword, required, username } from './fields.js';

export const loginSchema = z.object({
  login: required(),
  password: required(),
});

export const registerSchema = z
  .object({
    username: username(),
    email: email(),
    password: newPassword(),
    confirmPassword: z.string(),
    registrationCode: z.string().trim(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    path: ['confirmPassword'],
    error: () => m.validation_passwords_match(),
  });
