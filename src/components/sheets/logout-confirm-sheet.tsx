import { BottomSheetBackdrop, BottomSheetModal, BottomSheetView } from "@gorhom/bottom-sheet";
import type { ElementRef, RefObject } from "react";
import { useCallback, useMemo } from "react";
import { View } from "react-native";
import { useRouter } from "expo-router";
import { href } from "@/navigation/href";
import { PrimaryButton } from "@/components/buttons/primary-button";
import { AppText } from "@/components/typography/app-text";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { useAuthStore } from "@/stores/auth.store";
import { space } from "@/theme/spacing/tokens";

export type PlannerBottomSheetModalRef = ElementRef<typeof BottomSheetModal>;
type Props = { sheetRef: RefObject<PlannerBottomSheetModalRef | null> };

export function LogoutConfirmSheet({ sheetRef }: Props) {
  const c = usePlannerTheme();
  const router = useRouter();
  const logout = useAuthStore((s) => s.logout);

  const snapPoints = useMemo(() => ["32%"], []);

  const renderBackdrop = useCallback(
    (props: Parameters<typeof BottomSheetBackdrop>[0]) => (
      <BottomSheetBackdrop {...props} appearsOnIndex={0} disappearsOnIndex={-1} pressBehavior="close" />
    ),
    []
  );

  return (
    <BottomSheetModal
      ref={sheetRef}
      index={0}
      snapPoints={snapPoints}
      backdropComponent={renderBackdrop}
      backgroundStyle={{ backgroundColor: c.surfaceElevated }}
      handleIndicatorStyle={{ backgroundColor: c.border }}
    >
      <BottomSheetView style={{ padding: space.lg, gap: space.md }}>
        <AppText variant="title">Sign out?</AppText>
        <AppText variant="body" color="secondary">
          You can sign back in anytime. Local tokens on this device will be cleared.
        </AppText>
        <View style={{ gap: space.sm, marginTop: space.sm }}>
          <PrimaryButton
            title="Sign out"
            onPress={async () => {
              sheetRef.current?.dismiss();
              await logout();
              router.replace(href("/(public)/welcome"));
            }}
          />
          <PrimaryButton title="Cancel" variant="ghost" onPress={() => sheetRef.current?.dismiss()} />
        </View>
      </BottomSheetView>
    </BottomSheetModal>
  );
}
