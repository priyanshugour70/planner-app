import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { authApi } from "@/modules/auth/api/auth-api";
import {
  loginWithPassword,
  signupAccount,
  requestPasswordReset,
  resetPasswordWithCode,
  sendEmailOtp,
  verifyEmailOtp,
} from "@/modules/auth/services/auth-flow.service";
import { useAuthStore } from "@/stores/auth.store";
import type { AuthSessionPayload } from "@/types/auth";
import type { OtpPurpose } from "@/modules/auth/validators";

export function useLoginMutation() {
  const setSession = useAuthStore((s) => s.setSessionFromPayload);
  return useMutation({
    mutationFn: async (vars: { email: string; password: string }) => {
      const data = await loginWithPassword(vars.email, vars.password);
      if (!("accessToken" in data)) throw new Error("Invalid session response");
      return data as AuthSessionPayload;
    },
    onSuccess: async (data) => {
      await setSession(data);
    },
  });
}

export function useSignupMutation() {
  const setSession = useAuthStore((s) => s.setSessionFromPayload);
  return useMutation({
    mutationFn: signupAccount,
    onSuccess: async (data) => {
      await setSession(data);
    },
  });
}

export function useForgotPasswordMutation() {
  return useMutation({ mutationFn: (email: string) => requestPasswordReset(email) });
}

export function useResetPasswordMutation() {
  return useMutation({ mutationFn: resetPasswordWithCode });
}

export function useSendOtpMutation() {
  return useMutation({ mutationFn: (vars: { email: string; purpose: OtpPurpose }) => sendEmailOtp(vars.email, vars.purpose) });
}

export function useVerifyOtpMutation() {
  const setSession = useAuthStore((s) => s.setSessionFromPayload);
  return useMutation({
    mutationFn: async (vars: { email: string; code: string; purpose: OtpPurpose }) => {
      return verifyEmailOtp(vars.email, vars.code, vars.purpose);
    },
    onSuccess: async (data, vars) => {
      if (vars.purpose === "login" && "accessToken" in data) {
        await setSession(data as AuthSessionPayload);
      }
    },
  });
}

export function useSessionsQuery(enabled: boolean) {
  return useQuery({
    queryKey: ["auth", "sessions"],
    queryFn: () => authApi.sessions(),
    enabled,
  });
}

export function useRevokeSessionMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (sessionId: string) => authApi.revokeSession(sessionId),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["auth", "sessions"] });
    },
  });
}

export function useRevokeOthersMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => authApi.revokeOtherSessions(),
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ["auth", "sessions"] });
    },
  });
}
