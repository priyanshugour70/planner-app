import type { DimensionValue, StyleProp, ViewStyle } from "react-native";
import { View } from "react-native";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { radius } from "@/theme/radius/tokens";

type Props = {
  height: number;
  width?: DimensionValue;
  radiusPx?: number;
  style?: StyleProp<ViewStyle>;
};

export function SkeletonBlock({ height, width = "100%", radiusPx = radius.md, style }: Props) {
  const c = usePlannerTheme();
  return (
    <View
      style={[
        {
          height,
          width,
          borderRadius: radiusPx,
          backgroundColor: c.border,
          opacity: 0.55,
        },
        style,
      ]}
    />
  );
}

export function FinanceListSkeleton({ rows = 6 }: { rows?: number }) {
  return (
    <View style={{ gap: 10 }}>
      {Array.from({ length: rows }).map((_, i) => (
        <View key={i} style={{ gap: 8 }}>
          <SkeletonBlock height={18} width="55%" />
          <SkeletonBlock height={14} width="40%" />
          <SkeletonBlock height={22} width="35%" />
        </View>
      ))}
    </View>
  );
}
