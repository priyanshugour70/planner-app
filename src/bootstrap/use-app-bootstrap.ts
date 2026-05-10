import * as SplashScreen from "expo-splash-screen";
import { useEffect } from "react";
import { AccessibilityInfo, AppState, type AppStateStatus } from "react-native";
import { useAuthStore } from "@/stores/auth.store";
import { useUiStore } from "@/stores/ui.store";
import { apiGetJson } from "@/services/api/http-client";
import type { MeResponse } from "@/types/auth";

SplashScreen.preventAutoHideAsync().catch(() => {});

export function useAppBootstrap() {
  const bootstrap = useAuthStore((s) => s.bootstrap);

  useEffect(() => {
    AccessibilityInfo.isReduceMotionEnabled().then((v) => {
      useUiStore.getState().setReducedMotion(Boolean(v));
    });
    const sub = AccessibilityInfo.addEventListener("reduceMotionChanged", (v) => {
      useUiStore.getState().setReducedMotion(Boolean(v));
    });
    return () => sub.remove();
  }, []);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      await bootstrap();
      if (!cancelled) await SplashScreen.hideAsync();
    })();
    return () => {
      cancelled = true;
    };
  }, [bootstrap]);

  useEffect(() => {
    const onChange = async (state: AppStateStatus) => {
      if (state !== "active") return;
      if (!useAuthStore.getState().isAuthenticated) return;
      try {
        await apiGetJson<MeResponse>("/auth/me");
      } catch {
        await useAuthStore.getState().forceGuest();
        useAuthStore.getState().markSessionExpired();
      }
    };
    const sub = AppState.addEventListener("change", onChange);
    return () => sub.remove();
  }, []);

  return {};
}
