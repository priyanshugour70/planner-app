import { Stack } from "expo-router";
import { StackHeaderBack } from "@/components/navigation/stack-header-back";
import { MainMenuHeaderButton } from "@/navigation/main-menu-context";
import { usePlannerTheme } from "@/providers/planner-theme-provider";

export default function DebtStackLayout() {
  const c = usePlannerTheme();
  return (
    <Stack
      screenOptions={{
        headerShown: true,
        headerLargeTitle: false,
        headerBackVisible: false,
        headerStyle: { backgroundColor: c.surface },
        headerShadowVisible: false,
        headerTintColor: c.textPrimary,
        headerTitleStyle: { fontSize: 17 },
        headerLeft: () => <StackHeaderBack />,
        headerRight: () => <MainMenuHeaderButton />,
      }}
    >
      <Stack.Screen name="index" options={{ title: "Debt" }} />
      <Stack.Screen name="[id]" options={{ title: "Obligation" }} />
    </Stack>
  );
}
