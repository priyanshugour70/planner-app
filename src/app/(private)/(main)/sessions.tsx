import { useRouter } from "expo-router";
import { href } from "@/navigation/href";
import { Alert, FlatList, Pressable, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { PrimaryButton } from "@/components/buttons/primary-button";
import { EmptyState } from "@/components/ui/empty-state";
import { FinanceListSkeleton } from "@/components/ui/skeleton";
import { Screen } from "@/components/ui/screen";
import { AppText } from "@/components/typography/app-text";
import {
  useRevokeOthersMutation,
  useRevokeSessionMutation,
  useSessionsQuery,
} from "@/modules/auth/hooks/use-auth-mutations";
import type { AuthSessionRow } from "@/types/auth";
import { useAuthStore } from "@/stores/auth.store";
import { space } from "@/theme/spacing/tokens";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { radius } from "@/theme/radius/tokens";

export default function SessionsScreen() {
  const router = useRouter();
  const c = usePlannerTheme();
  const sessionId = useAuthStore((s) => s.sessionId);
  const { data, refetch, isRefetching, isPending } = useSessionsQuery(true);
  const revokeOne = useRevokeSessionMutation();
  const revokeOthers = useRevokeOthersMutation();

  const rows = data?.sessions ?? [];

  const onRevoke = (id: string) => {
    Alert.alert("Revoke session?", "You will be signed out on that device.", [
      { text: "Cancel", style: "cancel" },
      {
        text: "Revoke",
        style: "destructive",
        onPress: () => {
          void (async () => {
            try {
              if (id === sessionId) {
                await useAuthStore.getState().logout();
                router.replace(href("/(public)/welcome"));
                return;
              }
              await revokeOne.mutateAsync(id);
            } catch {
              Alert.alert("Could not revoke");
            }
          })();
        },
      },
    ]);
  };

  return (
    <Screen>
      <View style={{ flexDirection: "row", alignItems: "center", justifyContent: "flex-end" }}>
        <Pressable onPress={() => refetch()} hitSlop={12} style={{ flexDirection: "row", alignItems: "center", gap: space.xs }}>
          <Ionicons name="refresh" size={18} color={c.textMuted} />
          <AppText variant="caption" color="muted">
            {isRefetching ? "Refreshing…" : "Refresh"}
          </AppText>
        </Pressable>
      </View>
      <View style={{ marginTop: space.md, gap: space.sm }}>
        <AppText variant="title">Sessions</AppText>
        <AppText variant="body" color="secondary">
          Manage where your account stays signed in. Revoke anything you do not recognize.
        </AppText>
      </View>
      <View style={{ marginTop: space.md, flex: 1, minHeight: 2 }}>
        <FlatList
          data={rows}
          keyExtractor={(item) => item.id}
          refreshing={isRefetching}
          onRefresh={() => refetch()}
          ListEmptyComponent={
            isPending ? (
              <FinanceListSkeleton rows={4} />
            ) : (
              <EmptyState icon="phone-portrait-outline" title="No sessions" subtitle="When you sign in on other devices, they will appear here." />
            )
          }
          renderItem={({ item }: { item: AuthSessionRow }) => {
            const isThis = item.id === sessionId;
            return (
              <View
                style={{
                  padding: space.lg,
                  borderRadius: radius.lg,
                  borderWidth: 1,
                  borderColor: isThis ? c.accent : c.border,
                  backgroundColor: isThis ? c.accentSoft : c.card,
                  marginBottom: space.sm,
                }}
              >
                <View style={{ flexDirection: "row", justifyContent: "space-between", alignItems: "flex-start", gap: space.md }}>
                  <View style={{ flex: 1, minWidth: 0 }}>
                    <View style={{ flexDirection: "row", alignItems: "center", gap: space.sm, flexWrap: "wrap" }}>
                      <AppText variant="subtitle">{item.platform ?? "Device"}</AppText>
                      {isThis ? (
                        <View style={{ paddingHorizontal: space.sm, paddingVertical: 2, borderRadius: radius.sm, backgroundColor: c.accent }}>
                          <AppText variant="caption" style={{ color: "#0B0B10", fontWeight: "800" }}>
                            This device
                          </AppText>
                        </View>
                      ) : null}
                    </View>
                    <AppText variant="caption" color="muted" style={{ marginTop: space.xs }}>
                      {[item.browser, item.os].filter(Boolean).join(" · ")}
                    </AppText>
                    <AppText variant="caption" color={item.revoked ? "danger" : "success"} style={{ marginTop: space.xs, fontWeight: "600" }}>
                      {item.revoked ? "Revoked" : "Active"}
                    </AppText>
                  </View>
                  <Ionicons name="hardware-chip-outline" size={28} color={isThis ? c.accent : c.textMuted} />
                </View>
                {!item.revoked ? (
                  <Pressable style={{ marginTop: space.md, alignSelf: "flex-start" }} onPress={() => onRevoke(item.id)} hitSlop={8}>
                    <AppText variant="caption" color="danger">
                      Revoke
                    </AppText>
                  </Pressable>
                ) : null}
              </View>
            );
          }}
        />
      </View>
      <View style={{ paddingTop: space.md, gap: space.sm }}>
        <PrimaryButton
          title="Sign out other devices"
          variant="ghost"
          loading={revokeOthers.isPending}
          onPress={() =>
            Alert.alert("Sign out other devices?", undefined, [
              { text: "Cancel", style: "cancel" },
              { text: "Continue", style: "destructive", onPress: () => void revokeOthers.mutateAsync() },
            ])
          }
        />
      </View>
    </Screen>
  );
}
