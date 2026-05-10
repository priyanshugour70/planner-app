import { BottomSheetBackdrop, BottomSheetModal, BottomSheetView } from "@gorhom/bottom-sheet";
import { forwardRef, useCallback, useMemo, type ElementRef, type Ref } from "react";
import { Pressable, View } from "react-native";
import { usePathname, useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { href } from "@/navigation/href";
import { AppText } from "@/components/typography/app-text";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

export type MainNavMenuSheetRef = ElementRef<typeof BottomSheetModal>;

type Item = { key: string; label: string; path: string; icon: keyof typeof Ionicons.glyphMap };

const ITEMS: Item[] = [
  { key: "home", label: "Home", path: "/(private)/(main)", icon: "home-outline" },
  { key: "finance", label: "Finance", path: "/(private)/(main)/finance", icon: "wallet-outline" },
  { key: "tasks", label: "Tasks", path: "/(private)/(main)/tasks", icon: "checkbox-outline" },
  { key: "sessions", label: "Sessions", path: "/(private)/(main)/sessions", icon: "phone-portrait-outline" },
];

function navActive(pathname: string, item: Item): boolean {
  if (item.key === "finance") return pathname.includes("/finance");
  if (item.key === "tasks") return pathname.includes("/tasks");
  if (item.key === "sessions") return pathname.includes("/sessions");
  if (item.key === "home")
    return !pathname.includes("/finance") && !pathname.includes("/tasks") && !pathname.includes("/sessions");
  return false;
}

function dismissRef(ref: Ref<MainNavMenuSheetRef | null>) {
  if (ref && typeof ref !== "function") ref.current?.dismiss();
}

export const MainNavMenuSheet = forwardRef<MainNavMenuSheetRef>(function MainNavMenuSheet(_, ref) {
  const c = usePlannerTheme();
  const router = useRouter();
  const pathname = usePathname();
  const snapPoints = useMemo(() => ["48%"], []);

  const renderBackdrop = useCallback(
    (props: Parameters<typeof BottomSheetBackdrop>[0]) => (
      <BottomSheetBackdrop {...props} appearsOnIndex={0} disappearsOnIndex={-1} pressBehavior="close" />
    ),
    []
  );

  return (
    <BottomSheetModal
      ref={ref}
      index={0}
      snapPoints={snapPoints}
      backdropComponent={renderBackdrop}
      backgroundStyle={{ backgroundColor: c.surfaceElevated }}
      handleIndicatorStyle={{ backgroundColor: c.border }}
    >
      <BottomSheetView style={{ paddingHorizontal: space.lg, paddingBottom: space.xl, gap: space.xs }}>
        <AppText variant="subtitle" style={{ marginBottom: space.xs }}>
          Navigation
        </AppText>
        <AppText variant="caption" color="muted" style={{ marginBottom: space.md }}>
          Pick a section. Your current screen is highlighted.
        </AppText>
        {ITEMS.map((item) => {
          const active = navActive(pathname, item);
          return (
            <Pressable
              key={item.key}
              accessibilityRole="button"
              accessibilityState={{ selected: active }}
              onPress={() => {
                dismissRef(ref);
                router.replace(href(item.path));
              }}
              style={{
                flexDirection: "row",
                alignItems: "center",
                gap: space.md,
                paddingVertical: space.md,
                paddingHorizontal: space.md,
                borderRadius: radius.md,
                backgroundColor: active ? c.accentSoft : "transparent",
                borderWidth: active ? 1 : 0,
                borderColor: active ? c.accent : "transparent",
              }}
            >
              <Ionicons name={item.icon} size={22} color={active ? c.accent : c.textSecondary} />
              <AppText variant="body" color={active ? "accent" : "primary"} style={{ fontWeight: active ? "600" : "400" }}>
                {item.label}
              </AppText>
              {active ? (
                <View style={{ marginLeft: "auto" }}>
                  <Ionicons name="checkmark-circle" size={20} color={c.accent} />
                </View>
              ) : null}
            </Pressable>
          );
        })}
      </BottomSheetView>
    </BottomSheetModal>
  );
});
