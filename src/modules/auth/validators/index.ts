import { z } from "zod";

export const passwordSchema = z
  .string()
  .min(12, "At least 12 characters")
  .max(128)
  .regex(/[A-Z]/, "Include an uppercase letter")
  .regex(/[a-z]/, "Include a lowercase letter")
  .regex(/[0-9]/, "Include a number")
  .regex(/[^A-Za-z0-9]/, "Include a symbol");

export const emailSchema = z.string().trim().toLowerCase().email().max(320);

export const usernameSchema = z
  .string()
  .trim()
  .min(3)
  .max(64)
  .regex(/^[a-zA-Z0-9._-]+$/, "Letters, numbers, dot, underscore, hyphen only");

export const loginSchema = z.object({
  email: emailSchema,
  password: z.string().min(1, "Required").max(128),
});

export const signupSchema = z.object({
  username: usernameSchema,
  email: emailSchema,
  password: passwordSchema,
  fullName: z.string().trim().max(255).optional(),
});

export const forgotSchema = z.object({
  email: emailSchema,
});

export const resetPasswordSchema = z.object({
  email: emailSchema,
  code: z.string().min(4).max(32),
  newPassword: passwordSchema,
});

export const otpPurposeSchema = z.enum([
  "login",
  "email_verification",
  "password_reset",
  "phone_verification",
  "two_factor",
]);

export type OtpPurpose = z.infer<typeof otpPurposeSchema>;

export const sendOtpSchema = z.object({
  email: emailSchema,
  purpose: otpPurposeSchema,
});

export const verifyOtpSchema = z.object({
  email: emailSchema,
  code: z.string().min(4).max(32),
  purpose: otpPurposeSchema,
});
