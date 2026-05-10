import { Pressable } from "react-native";
import { useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { AppText } from "@/components/typography/app-text";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { space } from "@/theme/spacing/tokens";
import type { ViewStyle } from "react-native";

type Props = {
  label?: string;
  style?: ViewStyle;
};

/** In-content back control when `router.canGoBack()` — use when stack header back is hidden or unclear. */
export function ScreenBackLink({ label = "Back", style }: Props) {
  const router = useRouter();
  const c = usePlannerTheme();

  if (!router.canGoBack()) return null;

  return (
    <Pressable
      onPress={() => router.back()}
      hitSlop={12}
      style={[{ flexDirection: "row", alignItems: "center", alignSelf: "flex-start", gap: space.xs, marginBottom: space.sm }, style]}
      accessibilityRole="button"
      accessibilityLabel={label}
    >
      <Ionicons name="chevron-back" size={22} color={c.accent} />
      <AppText variant="subtitle" color="accent">
        {label}
      </AppText>
    </Pressable>
  );
}
