import { create } from "zustand";
import type { AuthSessionPayload, AuthUserPublic, MeResponse } from "@/types/auth";
import { tokenMemory } from "@/services/auth/token-memory";
import { clearRefreshToken, readRefreshToken, saveRefreshToken } from "@/services/storage/secure-tokens";
import { apiGetJson, apiPostJson } from "@/services/api/http-client";
import { performRefreshRotation } from "@/services/auth/refresh-session";

export type AuthBootstrapStatus = "idle" | "bootstrapping" | "ready";

type State = {
  bootstrapStatus: AuthBootstrapStatus;
  isAuthenticated: boolean;
  user: AuthUserPublic | null;
  me: MeResponse | null;
  sessionId: string | null;
  sessionExpired: boolean;
  setBootstrapStatus: (s: AuthBootstrapStatus) => void;
  applyRefreshedSession: (
    data: Pick<AuthSessionPayload, "accessToken" | "refreshToken" | "user" | "sessionId">
  ) => void;
  setSessionFromPayload: (payload: AuthSessionPayload) => Promise<void>;
  fetchMe: () => Promise<void>;
  bootstrap: () => Promise<void>;
  forceGuest: () => Promise<void>;
  logout: () => Promise<void>;
  markSessionExpired: () => void;
  clearSessionExpired: () => void;
};

export const useAuthStore = create<State>((set, get) => ({
  bootstrapStatus: "idle",
  isAuthenticated: false,
  user: null,
  me: null,
  sessionId: null,
  sessionExpired: false,

  setBootstrapStatus: (bootstrapStatus) => set({ bootstrapStatus }),

  applyRefreshedSession: (data) => {
    tokenMemory.set(data.accessToken);
    set({
      user: data.user,
      sessionId: data.sessionId,
      isAuthenticated: true,
      sessionExpired: false,
    });
  },

  setSessionFromPayload: async (payload) => {
    tokenMemory.set(payload.accessToken);
    if (payload.refreshToken) await saveRefreshToken(payload.refreshToken);
    set({
      user: payload.user,
      sessionId: payload.sessionId,
      isAuthenticated: true,
      sessionExpired: false,
    });
    await get().fetchMe();
  },

  fetchMe: async () => {
    const me = await apiGetJson<MeResponse>("/auth/me");
    set({
      me,
      user: {
        id: me.id,
        username: me.username,
        email: me.email,
        emailVerified: me.emailVerified,
        accountStatus: me.accountStatus,
      },
    });
  },

  bootstrap: async () => {
    if (get().bootstrapStatus === "bootstrapping") return;
    set({ bootstrapStatus: "bootstrapping" });
    try {
      const rt = await readRefreshToken();
      if (!rt) {
        set({ isAuthenticated: false, user: null, me: null, sessionId: null });
        return;
      }
      const ok = await performRefreshRotation();
      if (!ok) {
        await get().forceGuest();
        return;
      }
      try {
        await get().fetchMe();
      } catch {
        await get().forceGuest();
      }
    } finally {
      set({ bootstrapStatus: "ready" });
    }
  },

  forceGuest: async () => {
    tokenMemory.set(null);
    await clearRefreshToken();
    set({
      isAuthenticated: false,
      user: null,
      me: null,
      sessionId: null,
    });
  },

  logout: async () => {
    const refreshToken = await readRefreshToken();
    try {
      await apiPostJson<{ ok: true }>(
        "/auth/logout",
        refreshToken ? { refreshToken } : {},
        { skipAuthRefresh: true }
      );
    } catch {
      /* still clear local session */
    }
    await get().forceGuest();
  },

  markSessionExpired: () => set({ sessionExpired: true, isAuthenticated: false }),
  clearSessionExpired: () => set({ sessionExpired: false }),
}));
