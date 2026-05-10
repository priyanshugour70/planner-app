import { authApi } from "@/modules/auth/api/auth-api";
import { getOrCreateDeviceId } from "@/lib/device/device-id";
import type { AuthSessionPayload } from "@/types/auth";
import type { OtpPurpose } from "@/modules/auth/validators";

export async function loginWithPassword(email: string, password: string): Promise<AuthSessionPayload> {
  const deviceId = await getOrCreateDeviceId();
  return authApi.login({ email, password, deviceId });
}

export async function signupAccount(input: {
  username: string;
  email: string;
  password: string;
  fullName?: string;
}): Promise<AuthSessionPayload> {
  return authApi.signup(input);
}

export async function requestPasswordReset(email: string) {
  return authApi.forgotPassword({ email });
}

export async function resetPasswordWithCode(input: {
  email: string;
  code: string;
  newPassword: string;
}) {
  return authApi.resetPassword(input);
}

export async function sendEmailOtp(email: string, purpose: OtpPurpose) {
  return authApi.sendOtp({ email, purpose });
}

export async function verifyEmailOtp(email: string, code: string, purpose: OtpPurpose) {
  return authApi.verifyOtp({ email, code, purpose });
}
