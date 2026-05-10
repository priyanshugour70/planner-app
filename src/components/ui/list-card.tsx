import type { ReactNode } from "react";
import { Pressable, View, type ViewStyle } from "react-native";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import type { SemanticColors } from "@/theme/colors/semantic";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

export type ListCardTone = "income" | "expense" | "accent" | "warning" | "default";

type Props = {
  children: ReactNode;
  onPress?: () => void;
  /** Full-card very light tint (no left stripe). */
  tone?: ListCardTone;
  style?: ViewStyle;
};

function toneSurface(tone: ListCardTone | undefined, c: SemanticColors) {
  switch (tone) {
    case "income":
      return { bg: c.successSoft, border: c.success };
    case "expense":
      return { bg: c.dangerSoft, border: c.danger };
    case "warning":
      return { bg: c.warningSoft, border: c.warning };
    case "accent":
      return { bg: c.accentSoft, border: c.accent };
    default:
      return { bg: c.card, border: c.border };
  }
}

export function ListCard({ children, onPress, tone = "default", style }: Props) {
  const c = usePlannerTheme();
  const t = toneSurface(tone, c);
  const borderColor = tone === "default" ? t.border : `${t.border}55`;
  const inner = (
    <View
      style={{
        borderRadius: radius.lg,
        borderWidth: 1,
        borderColor,
        backgroundColor: t.bg,
        padding: space.md,
      }}
    >
      {children}
    </View>
  );

  if (onPress) {
    return (
      <Pressable onPress={onPress} style={[{ marginBottom: space.sm }, style]}>
        {inner}
      </Pressable>
    );
  }
  return <View style={[{ marginBottom: space.sm }, style]}>{inner}</View>;
}
