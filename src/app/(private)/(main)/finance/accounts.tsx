import { useEffect, useState } from "react";
import { Alert, FlatList, Modal, Pressable, ScrollView, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { PrimaryButton } from "@/components/buttons/primary-button";
import { AppTextField } from "@/components/inputs/app-text-field";
import { EmptyState } from "@/components/ui/empty-state";
import { ListCard } from "@/components/ui/list-card";
import { PillButton } from "@/components/ui/pill-button";
import { FinanceListSkeleton } from "@/components/ui/skeleton";
import { Screen } from "@/components/ui/screen";
import { AppText } from "@/components/typography/app-text";
import { useAccountMutations, useAccounts } from "@/modules/finance/hooks/use-finance-queries";
import type { FinanceAccountDTO } from "@/types/planner";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { useFinanceQuickAddStore } from "@/stores/finance-quick-add.store";
import type { ListCardTone } from "@/components/ui/list-card";
import type { SemanticColors } from "@/theme/colors/semantic";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

function accountVisual(
  kind: string,
  c: SemanticColors
): { icon: keyof typeof Ionicons.glyphMap; tone: ListCardTone } {
  const k = kind.toLowerCase();
  if (k.includes("bank") || k.includes("card")) return { icon: "card-outline", tone: "accent" };
  if (k.includes("cash") || k.includes("wallet")) return { icon: "cash-outline", tone: "income" };
  if (k.includes("invest") || k.includes("broker")) return { icon: "trending-up-outline", tone: "warning" };
  return { icon: "ellipse-outline", tone: "default" };
}

export default function FinanceAccountsScreen() {
  const c = usePlannerTheme();
  const list = useAccounts();
  const { create, patch, remove } = useAccountMutations();
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<FinanceAccountDTO | null>(null);
  const [name, setName] = useState("");
  const [kind, setKind] = useState("cash");
  const [currency, setCurrency] = useState("INR");

  const openCreate = () => {
    setEditing(null);
    setName("");
    setKind("cash");
    setCurrency("INR");
    setOpen(true);
  };

  const quickPending = useFinanceQuickAddStore((s) => s.pending);
  const quickClear = useFinanceQuickAddStore((s) => s.clear);
  useEffect(() => {
    if (!quickPending || quickPending.target !== "accounts") return;
    openCreate();
    quickClear();
  }, [quickPending?.id, quickPending?.target, quickClear]);

  const openEdit = (a: FinanceAccountDTO) => {
    setEditing(a);
    setName(a.name);
    setKind(a.kind);
    setCurrency(a.currency);
    setOpen(true);
  };

  const submit = async () => {
    const body: Record<string, unknown> = { name: name.trim(), kind: kind.trim(), currency: currency.trim().toUpperCase() };
    try {
      if (editing) await patch.mutateAsync({ id: editing.id, body });
      else await create.mutateAsync(body);
      setOpen(false);
      await list.refetch();
    } catch (e) {
      Alert.alert("Error", e instanceof Error ? e.message : "Failed");
    }
  };

  const data = list.data ?? [];

  return (
    <Screen showBackLink={false}>
      <View style={{ marginBottom: space.md }}>
        <View style={{ flexDirection: "row", alignItems: "center", justifyContent: "space-between", gap: space.md }}>
          <View style={{ flex: 1, minWidth: 0 }}>
            <AppText variant="subtitle">Accounts</AppText>
            <AppText variant="caption" color="secondary" style={{ marginTop: space.xs }}>
              Where money sits — cash, bank, or custom kinds.
            </AppText>
          </View>
          <PillButton label="Add" onPress={openCreate} icon={<Ionicons name="add" size={20} color="#0B0B10" />} />
        </View>
      </View>
      <FlatList
        style={{ flex: 1 }}
        data={data}
        keyExtractor={(x) => x.id}
        refreshing={list.isFetching}
        onRefresh={() => list.refetch()}
        ListEmptyComponent={
          list.isPending ? (
            <FinanceListSkeleton rows={4} />
          ) : (
            <EmptyState icon="wallet-outline" title="No accounts" subtitle="Add at least one account so transactions can reference where funds moved." />
          )
        }
        renderItem={({ item }) => {
          const v = accountVisual(item.kind, c);
          return (
            <ListCard onPress={() => openEdit(item)} tone={v.tone}>
              <View style={{ flexDirection: "row", alignItems: "center", gap: space.md }}>
                <View
                  style={{
                    width: 44,
                    height: 44,
                    borderRadius: radius.md,
                    alignItems: "center",
                    justifyContent: "center",
                    backgroundColor: c.accentSoft,
                  }}
                >
                  <Ionicons name={v.icon} size={22} color={c.accent} />
                </View>
                <View style={{ flex: 1, minWidth: 0 }}>
                  <AppText variant="subtitle">{item.name}</AppText>
                  <AppText variant="caption" color="secondary" style={{ marginTop: space.xs, textTransform: "capitalize" }}>
                    {item.kind} · {item.currency}
                  </AppText>
                </View>
              </View>
              <Pressable
                onPress={() =>
                  Alert.alert("Delete account?", item.name, [
                    { text: "Cancel", style: "cancel" },
                    { text: "Delete", style: "destructive", onPress: () => void remove.mutateAsync(item.id).then(() => list.refetch()) },
                  ])
                }
                style={{ marginTop: space.sm, alignSelf: "flex-start" }}
                hitSlop={8}
              >
                <AppText variant="caption" color="danger">
                  Delete
                </AppText>
              </Pressable>
            </ListCard>
          );
        }}
      />

      <Modal visible={open} animationType="slide" transparent>
        <View style={{ flex: 1, backgroundColor: c.overlay, justifyContent: "flex-end" }}>
          <View
            style={{
              backgroundColor: c.surface,
              padding: space.lg,
              borderTopLeftRadius: radius.xl,
              borderTopRightRadius: radius.xl,
              borderTopWidth: 3,
              borderTopColor: c.success,
            }}
          >
            <AppText variant="title" style={{ marginBottom: space.md }}>
              {editing ? "Edit account" : "New account"}
            </AppText>
            <ScrollView keyboardShouldPersistTaps="handled">
              <AppTextField label="Name" value={name} onChangeText={setName} />
              <View style={{ height: space.md }} />
              <AppTextField label="Kind (e.g. cash, bank)" value={kind} onChangeText={setKind} autoCapitalize="none" />
              <View style={{ height: space.md }} />
              <AppTextField label="Currency (3 letters)" value={currency} onChangeText={setCurrency} autoCapitalize="characters" maxLength={3} />
            </ScrollView>
            <View style={{ flexDirection: "row", gap: space.sm, marginTop: space.lg }}>
              <PrimaryButton title="Cancel" variant="ghost" onPress={() => setOpen(false)} style={{ flex: 1, minWidth: 0 }} />
              <PrimaryButton title="Save" loading={create.isPending || patch.isPending} onPress={() => void submit()} style={{ flex: 1, minWidth: 0 }} />
            </View>
          </View>
        </View>
      </Modal>
    </Screen>
  );
}
