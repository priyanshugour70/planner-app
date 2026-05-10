import type { ReactNode } from "react";
import { GestureHandlerRootView } from "react-native-gesture-handler";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { BottomSheetModalProvider } from "@gorhom/bottom-sheet";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import Toast from "react-native-toast-message";
import { PlannerThemeProvider } from "@/providers/planner-theme-provider";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, staleTime: 30_000 },
  },
});

export function AppProviders({ children }: { children: ReactNode }) {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <QueryClientProvider client={queryClient}>
          <PlannerThemeProvider>
            <BottomSheetModalProvider>{children}</BottomSheetModalProvider>
          </PlannerThemeProvider>
        </QueryClientProvider>
      </SafeAreaProvider>
      <Toast />
    </GestureHandlerRootView>
  );
}
