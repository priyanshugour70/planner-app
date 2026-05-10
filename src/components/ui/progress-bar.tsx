import { View } from "react-native";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

type Props = {
  /** 0–100 */
  value: number;
  /** When spend exceeds limit */
  overBudget?: boolean;
};

export function ProgressBar({ value, overBudget }: Props) {
  const c = usePlannerTheme();
  const w = Math.max(0, Math.min(100, value));
  const fill = overBudget ? c.danger : c.accent;
  const track = c.border;
  return (
    <View style={{ height: 8, borderRadius: radius.sm, backgroundColor: track, overflow: "hidden", marginTop: space.xs }}>
      <View style={{ width: `${w}%`, height: "100%", borderRadius: radius.sm, backgroundColor: fill }} />
    </View>
  );
}
