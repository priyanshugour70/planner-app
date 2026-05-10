import { useRouter } from "expo-router";
import { href } from "@/navigation/href";
import { useRef } from "react";
import { Pressable, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import type { PlannerBottomSheetModalRef } from "@/components/sheets/logout-confirm-sheet";
import { PrimaryButton } from "@/components/buttons/primary-button";
import { LogoutConfirmSheet } from "@/components/sheets/logout-confirm-sheet";
import { Screen } from "@/components/ui/screen";
import { AppText } from "@/components/typography/app-text";
import { useAuthStore } from "@/stores/auth.store";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

function ActionCard({
  title,
  subtitle,
  icon,
  tint,
  onPress,
}: {
  title: string;
  subtitle: string;
  icon: keyof typeof Ionicons.glyphMap;
  tint: string;
  onPress: () => void;
}) {
  const c = usePlannerTheme();
  return (
    <Pressable
      onPress={onPress}
      style={{
        flexDirection: "row",
        alignItems: "center",
        gap: space.md,
        padding: space.lg,
        borderRadius: radius.lg,
        backgroundColor: c.card,
        borderWidth: 1,
        borderColor: c.border,
      }}
    >
      <View
        style={{
          width: 52,
          height: 52,
          borderRadius: radius.md,
          alignItems: "center",
          justifyContent: "center",
          backgroundColor: c.accentSoft,
          borderWidth: 1,
          borderColor: tint + "44",
        }}
      >
        <Ionicons name={icon} size={26} color={tint} />
      </View>
      <View style={{ flex: 1, minWidth: 0 }}>
        <AppText variant="subtitle">{title}</AppText>
        <AppText variant="caption" color="secondary" style={{ marginTop: space.xs }}>
          {subtitle}
        </AppText>
      </View>
      <Ionicons name="chevron-forward" size={22} color={c.textMuted} />
    </Pressable>
  );
}

export default function HomeScreen() {
  const router = useRouter();
  const c = usePlannerTheme();
  const user = useAuthStore((s) => s.me ?? s.user);
  const logoutRef = useRef<PlannerBottomSheetModalRef>(null);

  return (
    <Screen scroll>
      <View
        style={{
          padding: space.lg,
          borderRadius: radius.lg,
          backgroundColor: c.accentSoft,
          borderWidth: 1,
          borderColor: c.accent + "44",
          marginBottom: space.lg,
        }}
      >
        <AppText variant="caption" color="accent" style={{ fontWeight: "700", letterSpacing: 0.6 }}>
          PLANNER
        </AppText>
        <AppText variant="title" style={{ marginTop: space.xs }}>
          Welcome back
        </AppText>
        <AppText variant="body" color="secondary" style={{ marginTop: space.sm }}>
          {user?.email}
        </AppText>
      </View>

      <AppText variant="subtitle" style={{ marginBottom: space.md }}>
        Quick links
      </AppText>
      <View style={{ gap: space.md }}>
        <ActionCard
          title="Finance"
          subtitle="Budgets, transactions, accounts, debt"
          icon="wallet-outline"
          tint={c.accent}
          onPress={() => router.push(href("/(private)/(main)/finance"))}
        />
        <ActionCard
          title="Tasks"
          subtitle="Goals, due dates, priorities"
          icon="checkbox-outline"
          tint={c.success}
          onPress={() => router.push(href("/(private)/(main)/tasks"))}
        />
        <ActionCard
          title="Devices & sessions"
          subtitle="See where you are signed in"
          icon="phone-portrait-outline"
          tint={c.warning}
          onPress={() => router.push(href("/(private)/(main)/sessions"))}
        />
      </View>

      <View style={{ marginTop: space.xl }}>
        <PrimaryButton title="Sign out" variant="ghost" onPress={() => logoutRef.current?.present()} />
      </View>
      <LogoutConfirmSheet sheetRef={logoutRef} />
    </Screen>
  );
}
