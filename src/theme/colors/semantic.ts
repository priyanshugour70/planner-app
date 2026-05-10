import type { ColorSchemeName } from "react-native";
import { palette } from "@/theme/colors/palette";

export type SemanticColors = {
  background: string;
  surface: string;
  surfaceElevated: string;
  border: string;
  textPrimary: string;
  textSecondary: string;
  textMuted: string;
  accent: string;
  accentSoft: string;
  danger: string;
  success: string;
  warning: string;
  successSoft: string;
  dangerSoft: string;
  warningSoft: string;
  card: string;
  inputBg: string;
  overlay: string;
};

export function getSemanticColors(scheme: ColorSchemeName | null | undefined): SemanticColors {
  const dark = scheme === "dark" || scheme == null;
  if (dark) {
    return {
      background: palette.neutral950,
      surface: palette.neutral900,
      surfaceElevated: palette.neutral850,
      border: palette.neutral700,
      textPrimary: palette.neutral50,
      textSecondary: palette.neutral200,
      textMuted: palette.neutral400,
      accent: palette.accent,
      accentSoft: palette.accentSoft,
      danger: palette.danger,
      success: palette.success,
      warning: palette.warning,
      successSoft: "rgba(61, 214, 140, 0.14)",
      dangerSoft: "rgba(240, 113, 103, 0.14)",
      warningSoft: "rgba(245, 193, 92, 0.16)",
      card: palette.neutral800,
      inputBg: palette.neutral850,
      overlay: palette.overlay,
    };
  }
  return {
    background: palette.neutral50,
    surface: "#FFFFFF",
    surfaceElevated: "#FFFFFF",
    border: palette.neutral100,
    textPrimary: palette.neutral900,
    textSecondary: palette.neutral500,
    textMuted: palette.neutral400,
    accent: palette.accent,
    accentSoft: "rgba(108, 140, 255, 0.12)",
    danger: palette.danger,
    success: palette.success,
    warning: palette.warning,
    successSoft: "rgba(61, 214, 140, 0.12)",
    dangerSoft: "rgba(240, 113, 103, 0.1)",
    warningSoft: "rgba(245, 193, 92, 0.18)",
    card: "#FFFFFF",
    inputBg: palette.neutral50,
    overlay: "rgba(15,15,20,0.35)",
  };
}
