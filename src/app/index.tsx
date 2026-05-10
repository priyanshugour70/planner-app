import { Redirect } from "expo-router";
import { ActivityIndicator, View } from "react-native";
import { href } from "@/navigation/href";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { useAuthStore } from "@/stores/auth.store";

export default function Index() {
  const c = usePlannerTheme();
  const bootstrapStatus = useAuthStore((s) => s.bootstrapStatus);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const expired = useAuthStore((s) => s.sessionExpired);

  if (bootstrapStatus !== "ready") {
    return (
      <View style={{ flex: 1, alignItems: "center", justifyContent: "center", backgroundColor: c.background }}>
        <ActivityIndicator color={c.accent} />
      </View>
    );
  }
  if (expired) return <Redirect href={href("/(public)/session-expired")} />;
  if (isAuthenticated) return <Redirect href={href("/(private)/(main)")} />;
  return <Redirect href={href("/(public)/welcome")} />;
}
