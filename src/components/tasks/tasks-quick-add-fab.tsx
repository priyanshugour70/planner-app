import { Pressable } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { useTasksQuickAddStore } from "@/stores/tasks-quick-add.store";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { space } from "@/theme/spacing/tokens";

/** Floating + above the safe area — matches finance FAB placement. */
export function TasksQuickAddFab() {
  const c = usePlannerTheme();
  const insets = useSafeAreaInsets();
  const requestOpenCreate = useTasksQuickAddStore((s) => s.requestOpenCreate);
  const bottom = Math.max(insets.bottom, space.md) + space.sm;

  return (
    <Pressable
      onPress={() => requestOpenCreate()}
      accessibilityRole="button"
      accessibilityLabel="New task"
      style={{
        position: "absolute",
        right: space.lg,
        bottom,
        width: 56,
        height: 56,
        borderRadius: 28,
        backgroundColor: c.accent,
        alignItems: "center",
        justifyContent: "center",
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.2,
        shadowRadius: 8,
        elevation: 6,
      }}
    >
      <Ionicons name="add" size={30} color="#0B0B10" />
    </Pressable>
  );
}
