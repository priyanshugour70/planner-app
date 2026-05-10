import { Pressable } from "react-native";
import { useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { href } from "@/navigation/href";
import { usePlannerTheme } from "@/providers/planner-theme-provider";

type Props = {
  /** When `router.canGoBack()` is false, navigate here (default: main home). */
  fallbackHref?: string;
  accessibilityLabel?: string;
};

/** Chevron in the native stack header: pops when possible, otherwise replaces fallback. */
export function StackHeaderBack({
  fallbackHref = "/(private)/(main)",
  accessibilityLabel = "Back",
}: Props) {
  const router = useRouter();
  const c = usePlannerTheme();
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={accessibilityLabel}
      onPress={() => {
        if (router.canGoBack()) router.back();
        else router.replace(href(fallbackHref));
      }}
      hitSlop={12}
      style={{ paddingRight: 4, paddingVertical: 4, flexDirection: "row", alignItems: "center" }}
    >
      <Ionicons name="chevron-back" size={28} color={c.textPrimary} />
    </Pressable>
  );
}
