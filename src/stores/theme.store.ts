import AsyncStorage from "@react-native-async-storage/async-storage";
import type { ColorSchemeName } from "react-native";
import { create } from "zustand";
import { createJSONStorage, persist } from "zustand/middleware";

export type ThemePreference = "system" | "light" | "dark";

type State = {
  preference: ThemePreference;
  setPreference: (p: ThemePreference) => void;
};

export function resolveColorScheme(
  preference: ThemePreference,
  system: ColorSchemeName | null | undefined
): "light" | "dark" {
  if (preference === "system") return system === "dark" ? "dark" : "light";
  return preference;
}

export const useThemeStore = create(
  persist<State>(
    (set) => ({
      preference: "system",
      setPreference: (preference) => set({ preference }),
    }),
    {
      name: "planner.theme",
      storage: createJSONStorage(() => AsyncStorage),
      partialize: (s: State) => ({ preference: s.preference }),
    } as never
  )
);
