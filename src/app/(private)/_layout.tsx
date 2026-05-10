import { Redirect, Stack } from "expo-router";
import { href } from "@/navigation/href";
import { useAuthStore } from "@/stores/auth.store";

export default function PrivateLayout() {
  const ready = useAuthStore((s) => s.bootstrapStatus === "ready");
  const authed = useAuthStore((s) => s.isAuthenticated);
  if (!ready) return null;
  if (!authed) return <Redirect href={href("/(public)/welcome")} />;
  return (
    <Stack screenOptions={{ headerShown: false, animation: "fade" }}>
      <Stack.Screen name="(main)" />
    </Stack>
  );
}
