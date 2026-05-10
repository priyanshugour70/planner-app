import type { ReactNode } from "react";
import { ScrollView, View, type ViewStyle } from "react-native";
import { useHeaderHeight } from "@react-navigation/elements";
import { SafeAreaView, type Edge } from "react-native-safe-area-context";
import { ScreenBackLink } from "@/components/navigation/screen-back-link";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { space } from "@/theme/spacing/tokens";

type Props = {
  children: ReactNode;
  scroll?: boolean;
  style?: ViewStyle;
  /** When true (default), show an in-content Back control when navigation can go back. */
  showBackLink?: boolean;
};

export function Screen({ children, scroll, style, showBackLink = true }: Props) {
  const c = usePlannerTheme();
  /** When a native stack header is shown, top safe-area is already handled — omitting `top` avoids a large double gap. */
  const headerHeight = useHeaderHeight();
  const underStackHeader = headerHeight > 0;
  const safeEdges: readonly Edge[] = underStackHeader ? ["left", "right", "bottom"] : ["top", "left", "right", "bottom"];
  const padH = space.lg;
  const padTop = underStackHeader ? space.xs : space.sm;
  const padBottom = scroll ? space.xxxl : space.lg;
  const inner = (
    <>
      {showBackLink ? <ScreenBackLink /> : null}
      {children}
    </>
  );
  const body = scroll ? (
    <ScrollView
      keyboardShouldPersistTaps="handled"
      contentContainerStyle={{
        paddingHorizontal: padH,
        paddingTop: padTop,
        paddingBottom: padBottom,
      }}
      showsVerticalScrollIndicator={false}
    >
      {inner}
    </ScrollView>
  ) : (
    <View style={{ flex: 1, paddingHorizontal: padH, paddingTop: padTop, paddingBottom: space.md }}>{inner}</View>
  );
  return (
    <SafeAreaView style={[{ flex: 1, backgroundColor: c.background }, style]} edges={safeEdges}>
      {body}
    </SafeAreaView>
  );
}
