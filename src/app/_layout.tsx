import "../styles/reanimated";
import { Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { useAppBootstrap } from "@/bootstrap/use-app-bootstrap";
import { AppProviders } from "@/providers/app-providers";
import { resolveColorScheme, useThemeStore } from "@/stores/theme.store";
import { useColorScheme } from "react-native";

export default function RootLayout() {
  useAppBootstrap();
  const preference = useThemeStore((s) => s.preference);
  const system = useColorScheme();
  const scheme = resolveColorScheme(preference, system);

  return (
    <AppProviders>
      <StatusBar style={scheme === "dark" ? "light" : "dark"} />
      <Stack screenOptions={{ headerShown: false, animation: "fade" }}>
        <Stack.Screen name="index" />
        <Stack.Screen name="(public)" />
        <Stack.Screen name="(private)" />
      </Stack>
    </AppProviders>
  );
}
