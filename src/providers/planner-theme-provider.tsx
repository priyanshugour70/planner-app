import type { ReactNode } from "react";
import { createContext, useContext, useMemo } from "react";
import { useColorScheme } from "react-native";
import type { SemanticColors } from "@/theme/colors/semantic";
import { getSemanticColors } from "@/theme/colors/semantic";
import { resolveColorScheme, useThemeStore } from "@/stores/theme.store";

const Ctx = createContext<SemanticColors | null>(null);

export function PlannerThemeProvider({ children }: { children: ReactNode }) {
  const preference = useThemeStore((s) => s.preference);
  const system = useColorScheme();
  const scheme = resolveColorScheme(preference, system);
  const colors = useMemo(() => getSemanticColors(scheme), [scheme]);

  return <Ctx.Provider value={colors}>{children}</Ctx.Provider>;
}

export function usePlannerTheme(): SemanticColors {
  const v = useContext(Ctx);
  if (!v) throw new Error("usePlannerTheme must be used within PlannerThemeProvider");
  return v;
}
