import { View } from "react-native";
import { Stack } from "expo-router";
import { FinanceQuickAddFab } from "@/components/finance/finance-quick-add-fab";
import { StackHeaderBack } from "@/components/navigation/stack-header-back";
import { MainMenuHeaderButton } from "@/navigation/main-menu-context";
import { usePlannerTheme } from "@/providers/planner-theme-provider";

export default function FinanceStackLayout() {
  const c = usePlannerTheme();
  return (
    <View style={{ flex: 1 }}>
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
      <Stack.Screen name="index" options={{ title: "Finance" }} />
      <Stack.Screen name="transactions" options={{ title: "Transactions" }} />
      <Stack.Screen name="budgets" options={{ title: "Budgets" }} />
      <Stack.Screen name="accounts" options={{ title: "Accounts" }} />
      <Stack.Screen name="categories" options={{ title: "Categories" }} />
      <Stack.Screen name="recurring" options={{ title: "EMI & recurring" }} />
      <Stack.Screen name="debt" options={{ headerShown: false }} />
    </Stack>
    <FinanceQuickAddFab />
    </View>
  );
}
