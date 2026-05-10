import type { ReactNode } from "react";
import { Text, type TextProps, type TextStyle } from "react-native";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { typography } from "@/theme/typography/tokens";

type Variant = keyof typeof typography;

type Props = TextProps & {
  variant?: Variant;
  color?: "primary" | "secondary" | "muted" | "accent" | "danger" | "success" | "warning";
  children: ReactNode;
};

export function AppText({ variant = "body", color = "primary", style, children, ...rest }: Props) {
  const c = usePlannerTheme();
  const colorMap: Record<NonNullable<Props["color"]>, string> = {
    primary: c.textPrimary,
    secondary: c.textSecondary,
    muted: c.textMuted,
    accent: c.accent,
    danger: c.danger,
    success: c.success,
    warning: c.warning,
  };
  return (
    <Text style={[typography[variant], { color: colorMap[color] }, style as TextStyle]} {...rest}>
      {children}
    </Text>
  );
}
