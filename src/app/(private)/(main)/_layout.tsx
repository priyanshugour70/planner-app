import { useRef } from "react";
import { Stack } from "expo-router";
import type { MainNavMenuSheetRef } from "@/components/navigation/main-nav-menu-sheet";
import { MainNavMenuSheet } from "@/components/navigation/main-nav-menu-sheet";
import { MainMenuHeaderButton, MainMenuRefProvider } from "@/navigation/main-menu-context";
import { usePlannerTheme } from "@/providers/planner-theme-provider";

export default function MainStackLayout() {
  const c = usePlannerTheme();
  const menuRef = useRef<MainNavMenuSheetRef>(null);

  return (
    <MainMenuRefProvider menuRef={menuRef}>
      <Stack
        screenOptions={{
          headerShown: true,
          headerLargeTitle: false,
          headerStyle: { backgroundColor: c.surface },
          headerShadowVisible: false,
          headerTintColor: c.textPrimary,
          headerTitleStyle: { fontSize: 17 },
          headerRight: () => <MainMenuHeaderButton />,
        }}
      >
        <Stack.Screen name="index" options={{ title: "Home" }} />
        <Stack.Screen name="finance" options={{ headerShown: false }} />
        <Stack.Screen name="tasks" options={{ headerShown: false }} />
        <Stack.Screen name="sessions" options={{ title: "Sessions", headerBackVisible: false }} />
      </Stack>
      <MainNavMenuSheet ref={menuRef} />
    </MainMenuRefProvider>
  );
}
