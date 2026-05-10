import type { ReactNode } from "react";
import { Pressable } from "react-native";
import { AppText } from "@/components/typography/app-text";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

type Props = {
  label: string;
  onPress: () => void;
  icon?: ReactNode;
};

export function PillButton({ label, onPress, icon }: Props) {
  const c = usePlannerTheme();
  return (
    <Pressable
      onPress={onPress}
      accessibilityRole="button"
      style={{
        flexDirection: "row",
        alignItems: "center",
        gap: space.xs,
        paddingVertical: space.sm,
        paddingHorizontal: space.md,
        borderRadius: radius.lg,
        backgroundColor: c.accent,
      }}
    >
      {icon}
      <AppText variant="body" style={{ color: "#0B0B10", fontWeight: "700" }}>
        {label}
      </AppText>
    </Pressable>
  );
}
