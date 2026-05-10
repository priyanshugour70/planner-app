import type { ReactNode } from "react";
import { View } from "react-native";
import { AppText } from "@/components/typography/app-text";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import type { SemanticColors } from "@/theme/colors/semantic";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

export type StatTone = "income" | "spend" | "neutral" | "accent" | "warning";

type Props = {
  label: string;
  value: ReactNode;
  tone?: StatTone;
  footer?: string;
};

function toneStyle(tone: StatTone | undefined, c: SemanticColors) {
  switch (tone) {
    case "income":
      return { bg: c.successSoft, border: c.success, labelTint: "success" as const };
    case "spend":
      return { bg: c.dangerSoft, border: c.danger, labelTint: "danger" as const };
    case "warning":
      return { bg: c.warningSoft, border: c.warning, labelTint: "warning" as const };
    case "accent":
      return { bg: c.accentSoft, border: c.accent, labelTint: "accent" as const };
    default:
      return { bg: c.card, border: c.border, labelTint: "secondary" as const };
  }
}

export function StatTile({ label, value, tone = "neutral", footer }: Props) {
  const c = usePlannerTheme();
  const t = toneStyle(tone, c);
  const thickLeft = tone !== "neutral";
  return (
    <View
      style={{
        flex: 1,
        minWidth: "45%",
        padding: space.md,
        borderRadius: radius.lg,
        backgroundColor: t.bg,
        borderWidth: 1,
        borderColor: t.border,
        borderLeftWidth: thickLeft ? 3 : 1,
        borderLeftColor: thickLeft ? t.border : c.border,
      }}
    >
      <AppText variant="caption" color={t.labelTint === "secondary" ? "secondary" : t.labelTint}>
        {label}
      </AppText>
      <View style={{ marginTop: space.xs }}>{typeof value === "string" ? <AppText variant="title">{value}</AppText> : value}</View>
      {footer ? (
        <AppText variant="caption" color="muted" style={{ marginTop: space.xs }}>
          {footer}
        </AppText>
      ) : null}
    </View>
  );
}
