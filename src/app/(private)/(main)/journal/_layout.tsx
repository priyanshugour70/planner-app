import { Pressable, View } from "react-native";
import { Stack, useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { href } from "@/navigation/href";
import { MainMenuHeaderButton } from "@/navigation/main-menu-context";
import { usePlannerTheme } from "@/providers/planner-theme-provider";

function JournalHeaderBack() {
  const router = useRouter();
  const c = usePlannerTheme();
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel="Back to home"
      onPress={() => router.replace(href("/(private)/(main)"))}
      hitSlop={12}
      style={{ paddingRight: 4, paddingVertical: 4, flexDirection: "row", alignItems: "center" }}
    >
      <Ionicons name="chevron-back" size={28} color={c.textPrimary} />
    </Pressable>
  );
}

export default function JournalStackLayout() {
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
          headerLeft: () => <JournalHeaderBack />,
          headerRight: () => <MainMenuHeaderButton />,
        }}
      >
        <Stack.Screen name="index" options={{ title: "Journal" }} />
      </Stack>
    </View>
  );
}
