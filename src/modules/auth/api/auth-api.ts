import { apiDeleteJson, apiGetJson, apiPostJson } from "@/services/api/http-client";
import type { AuthSessionPayload, AuthSessionRow, MeResponse } from "@/types/auth";
import type { OtpPurpose } from "@/modules/auth/validators";

export type SendOtpResponse = { ok: true; message: string; devOtp?: string };
export type ForgotResponse = { ok: true; message: string; devOtp?: string };
export type ResetPasswordResponse = { ok: true; message: string };
export type LogoutResponse = { ok: true };

export const authApi = {
  login(body: { email: string; password: string; deviceId?: string }) {
    return apiPostJson<AuthSessionPayload>("/auth/login", body, { skipAuthRefresh: true });
  },
  signup(body: { username: string; email: string; password: string; fullName?: string }) {
    return apiPostJson<AuthSessionPayload>("/auth/signup", body, { skipAuthRefresh: true });
  },
  me() {
    return apiGetJson<MeResponse>("/auth/me");
  },
  logout(body: { refreshToken?: string }) {
    return apiPostJson<LogoutResponse>("/auth/logout", body, { skipAuthRefresh: true });
  },
  forgotPassword(body: { email: string }) {
    return apiPostJson<ForgotResponse>("/auth/forgot-password", body, { skipAuthRefresh: true });
  },
  resetPassword(body: { email: string; code: string; newPassword: string }) {
    return apiPostJson<ResetPasswordResponse>("/auth/reset-password", body, { skipAuthRefresh: true });
  },
  sendOtp(body: { email: string; purpose: OtpPurpose }) {
    return apiPostJson<SendOtpResponse>("/auth/send-otp", body, { skipAuthRefresh: true });
  },
  resendOtp(body: { email: string; purpose: OtpPurpose }) {
    return apiPostJson<SendOtpResponse>("/auth/resend-otp", body, { skipAuthRefresh: true });
  },
  verifyOtp(body: { email: string; code: string; purpose: OtpPurpose }) {
    return apiPostJson<AuthSessionPayload | { ok: true; message: string }>("/auth/verify-otp", body, {
      skipAuthRefresh: true,
    });
  },
  sessions() {
    return apiGetJson<{ sessions: AuthSessionRow[] }>("/auth/sessions");
  },
  revokeSession(sessionId: string) {
    return apiDeleteJson<{ ok: true }>(`/auth/sessions/${sessionId}`);
  },
  revokeOtherSessions() {
    return apiPostJson<{ ok: true }>("/auth/sessions/revoke-others", {});
  },
};
