import { useState } from "react";
import { Modal, Pressable, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { href } from "@/navigation/href";
import { useFinanceQuickAddStore, type FinanceQuickAddTarget } from "@/stores/finance-quick-add.store";
import { AppText } from "@/components/typography/app-text";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

type Choice = {
  target: FinanceQuickAddTarget;
  title: string;
  subtitle: string;
  path: string;
};

const CHOICES: Choice[] = [
  {
    target: "transaction",
    title: "Log a transaction",
    subtitle: "Income or expense you just paid or received",
    path: "/(private)/(main)/finance/transactions",
  },
  {
    target: "budget",
    title: "New budget",
    subtitle: "Cap spending for a period",
    path: "/(private)/(main)/finance/budgets",
  },
  {
    target: "accounts",
    title: "New account",
    subtitle: "Bank, cash, card, or wallet",
    path: "/(private)/(main)/finance/accounts",
  },
  {
    target: "categories",
    title: "New category",
    subtitle: "Label for reporting",
    path: "/(private)/(main)/finance/categories",
  },
  {
    target: "debt",
    title: "Debt or receivable",
    subtitle: "Money you owe or someone owes you",
    path: "/(private)/(main)/finance/debt",
  },
  {
    target: "recurring",
    title: "Recurring / EMI",
    subtitle: "Rent, loan EMI, salary template",
    path: "/(private)/(main)/finance/recurring",
  },
];

export function FinanceQuickAddFab() {
  const c = usePlannerTheme();
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const request = useFinanceQuickAddStore((s) => s.request);
  const [menuOpen, setMenuOpen] = useState(false);

  const onPick = (choice: Choice) => {
    setMenuOpen(false);
    request(choice.target);
    router.push(href(choice.path));
  };

  const bottom = Math.max(insets.bottom, space.md) + space.sm;

  return (
    <>
      <Pressable
        onPress={() => setMenuOpen(true)}
        accessibilityRole="button"
        accessibilityLabel="Quick add in finance"
        style={{
          position: "absolute",
          right: space.lg,
          bottom,
          width: 56,
          height: 56,
          borderRadius: 28,
          backgroundColor: c.accent,
          alignItems: "center",
          justifyContent: "center",
          shadowColor: "#000",
          shadowOffset: { width: 0, height: 4 },
          shadowOpacity: 0.2,
          shadowRadius: 8,
          elevation: 6,
        }}
      >
        <Ionicons name="add" size={30} color="#0B0B10" />
      </Pressable>

      <Modal visible={menuOpen} animationType="fade" transparent onRequestClose={() => setMenuOpen(false)}>
        <Pressable style={{ flex: 1, backgroundColor: c.overlay, justifyContent: "flex-end" }} onPress={() => setMenuOpen(false)}>
          <Pressable
            onPress={(e) => e.stopPropagation()}
            style={{
              backgroundColor: c.surface,
              paddingHorizontal: space.lg,
              paddingTop: space.lg,
              paddingBottom: bottom,
              borderTopLeftRadius: radius.xl,
              borderTopRightRadius: radius.xl,
              borderTopWidth: 3,
              borderTopColor: c.accent,
            }}
          >
            <AppText variant="title" style={{ marginBottom: space.xs }}>
              What would you like to add?
            </AppText>
            <AppText variant="caption" color="secondary" style={{ marginBottom: space.md }}>
              Pick one — we will open the right screen and the form for you.
            </AppText>
            <View style={{ gap: space.sm }}>
              {CHOICES.map((choice) => (
                <Pressable
                  key={choice.target}
                  onPress={() => onPick(choice)}
                  style={{
                    paddingVertical: space.md,
                    paddingHorizontal: space.md,
                    borderRadius: radius.lg,
                    borderWidth: 1,
                    borderColor: c.border,
                    backgroundColor: c.card,
                  }}
                >
                  <AppText variant="subtitle">{choice.title}</AppText>
                  <AppText variant="caption" color="secondary" style={{ marginTop: 4 }}>
                    {choice.subtitle}
                  </AppText>
                </Pressable>
              ))}
            </View>
            <Pressable onPress={() => setMenuOpen(false)} style={{ marginTop: space.md, paddingVertical: space.sm, alignItems: "center" }}>
              <AppText variant="body" color="accent">
                Cancel
              </AppText>
            </Pressable>
          </Pressable>
        </Pressable>
      </Modal>
    </>
  );
}
