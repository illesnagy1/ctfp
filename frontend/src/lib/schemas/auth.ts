import { z } from 'zod';

import { m } from '#lib/paraglide/messages.js';

import { zForgotPasswordRequest, zLoginRequest, zRegisterRequest, zResetPasswordRequest } from './api.js';
import { email, trimmed, username } from './fields.js';

export const loginSchema = zLoginRequest;

const passwordsMatch = (data: { password: string; confirmPassword: string }) => data.password === data.confirmPassword;
const mismatch = { path: ['confirmPassword'], error: () => m.validation_passwords_match() };

export const registerSchema = zRegisterRequest
  .extend({
    username,
    email,
    confirmPassword: z.string(),
    // Empty for none.
    registrationCode: trimmed(zRegisterRequest.shape.registrationCode.unwrap().unwrap()),
  })
  .refine(passwordsMatch, mismatch);

export const forgotPasswordSchema = zForgotPasswordRequest.extend({ email });

export const resetPasswordSchema = zResetPasswordRequest
  // The token comes from the emailed link, carried in a hidden field.
  .extend({ confirmPassword: z.string() })
  .refine(passwordsMatch, mismatch);
