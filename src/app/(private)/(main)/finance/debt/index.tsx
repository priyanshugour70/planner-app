import { useRouter } from "expo-router";
import { useEffect, useState } from "react";
import { Alert, FlatList, Modal, Pressable, ScrollView, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { href } from "@/navigation/href";
import { PrimaryButton } from "@/components/buttons/primary-button";
import { AppTextField } from "@/components/inputs/app-text-field";
import { EmptyState } from "@/components/ui/empty-state";
import { ListCard } from "@/components/ui/list-card";
import { PillButton } from "@/components/ui/pill-button";
import { FinanceListSkeleton } from "@/components/ui/skeleton";
import { Screen } from "@/components/ui/screen";
import { AppText } from "@/components/typography/app-text";
import { formatInrAmount } from "@/lib/format-inr";
import { useDebtMutations, useObligations } from "@/modules/finance/hooks/use-finance-queries";
import { todayISO } from "@/modules/finance/utils/dates";
import type { DebtObligationDTO } from "@/types/planner";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { useFinanceQuickAddStore } from "@/stores/finance-quick-add.store";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

export default function DebtObligationsScreen() {
  const router = useRouter();
  const c = usePlannerTheme();
  const list = useObligations();
  const { createObligation, deleteObligation } = useDebtMutations();
  const [open, setOpen] = useState(false);
  const [counterparty, setCounterparty] = useState("");
  const [direction, setDirection] = useState<"owed" | "lent">("owed");
  const [principal, setPrincipal] = useState("");
  const [dueDate, setDueDate] = useState("");
  const [notes, setNotes] = useState("");

  const openDebtCreate = () => {
    setCounterparty("");
    setDirection("owed");
    setPrincipal("");
    setDueDate("");
    setNotes("");
    setOpen(true);
  };

  const quickPending = useFinanceQuickAddStore((s) => s.pending);
  const quickClear = useFinanceQuickAddStore((s) => s.clear);
  useEffect(() => {
    if (!quickPending || quickPending.target !== "debt") return;
    openDebtCreate();
    quickClear();
  }, [quickPending?.id, quickPending?.target, quickClear]);

  const submit = async () => {
    const body: Record<string, unknown> = {
      counterparty: counterparty.trim(),
      direction,
      principal,
      currency: "INR",
      dueDate: dueDate.trim() || null,
      status: "open",
      notes: notes.trim() || null,
    };
    try {
      await createObligation.mutateAsync(body);
      setOpen(false);
      setCounterparty("");
      setPrincipal("");
      setDueDate("");
      setNotes("");
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
            <AppText variant="subtitle">Debt & receivables</AppText>
            <View style={{ flexDirection: "row", flexWrap: "wrap", marginTop: space.xs, gap: 0 }}>
              <AppText variant="caption" color="danger" style={{ fontWeight: "700" }}>
                Owed
              </AppText>
              <AppText variant="caption" color="secondary">
                {" = you repay · "}
              </AppText>
              <AppText variant="caption" color="success" style={{ fontWeight: "700" }}>
                Lent
              </AppText>
              <AppText variant="caption" color="secondary">
                {" = they repay you. Tap a row for payments."}
              </AppText>
            </View>
          </View>
          <PillButton label="Add" onPress={() => setOpen(true)} icon={<Ionicons name="add" size={20} color="#0B0B10" />} />
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
            <EmptyState
              icon="hand-left-outline"
              title="No obligations"
              subtitle="Track informal loans: money you owe someone or they owe you, with optional due dates."
            />
          )
        }
        renderItem={({ item }: { item: DebtObligationDTO }) => {
          const tone = item.direction === "lent" ? "income" : "expense";
          return (
            <ListCard onPress={() => router.push(href(`/(private)/(main)/finance/debt/${item.id}`))} tone={tone}>
              <View style={{ flexDirection: "row", justifyContent: "space-between", alignItems: "flex-start", gap: space.md }}>
                <View style={{ flex: 1, minWidth: 0 }}>
                  <AppText variant="subtitle" numberOfLines={2}>
                    {item.counterparty}
                  </AppText>
                  <View style={{ flexDirection: "row", alignItems: "center", gap: space.sm, marginTop: space.xs, flexWrap: "wrap" }}>
                    <View
                      style={{
                        paddingHorizontal: space.sm,
                        paddingVertical: 2,
                        borderRadius: radius.sm,
                        backgroundColor: item.direction === "lent" ? c.successSoft : c.dangerSoft,
                      }}
                    >
                      <AppText variant="caption" color={item.direction === "lent" ? "success" : "danger"} style={{ fontWeight: "700", textTransform: "capitalize" }}>
                        {item.direction}
                      </AppText>
                    </View>
                    <AppText variant="caption" color="secondary" style={{ textTransform: "capitalize" }}>
                      {item.status}
                    </AppText>
                  </View>
                  <AppText variant="caption" color="muted" style={{ marginTop: space.xs }}>
                    Due {(item.dueDate ?? "—").slice(0, 10)}
                  </AppText>
                </View>
                <AppText variant="subtitle" color={item.direction === "lent" ? "success" : "danger"}>
                  {formatInrAmount(item.balance)}
                </AppText>
              </View>
              <Pressable
                onPress={() =>
                  Alert.alert("Delete obligation?", item.counterparty, [
                    { text: "Cancel", style: "cancel" },
                    {
                      text: "Delete",
                      style: "destructive",
                      onPress: () => void deleteObligation.mutateAsync(item.id).then(() => list.refetch()),
                    },
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
              borderTopColor: c.accent,
            }}
          >
            <AppText variant="title" style={{ marginBottom: space.md }}>
              New debt / receivable
            </AppText>
            <ScrollView keyboardShouldPersistTaps="handled">
              <AppTextField label="Counterparty" value={counterparty} onChangeText={setCounterparty} />
              <View style={{ height: space.md }} />
              <AppText variant="caption" color="secondary">
                Direction
              </AppText>
              <View style={{ flexDirection: "row", gap: space.sm, marginTop: space.xs }}>
                {(["owed", "lent"] as const).map((d) => (
                  <Pressable
                    key={d}
                    onPress={() => setDirection(d)}
                    style={{
                      flex: 1,
                      padding: space.sm,
                      borderRadius: radius.md,
                      borderWidth: 1,
                      borderColor: direction === d ? (d === "lent" ? c.success : c.danger) : c.border,
                      backgroundColor: direction === d ? (d === "lent" ? c.successSoft : c.dangerSoft) : "transparent",
                    }}
                  >
                    <AppText
                      variant="body"
                      color={direction === d ? (d === "lent" ? "success" : "danger") : "primary"}
                      style={{ textAlign: "center", textTransform: "capitalize" }}
                    >
                      {d}
                    </AppText>
                  </Pressable>
                ))}
              </View>
              <View style={{ height: space.md }} />
              <AppTextField label="Principal" value={principal} onChangeText={setPrincipal} keyboardType="decimal-pad" />
              <View style={{ height: space.md }} />
              <AppTextField label="Due date YYYY-MM-DD (optional)" value={dueDate} onChangeText={setDueDate} placeholder={todayISO()} />
              <View style={{ height: space.md }} />
              <AppTextField label="Notes" value={notes} onChangeText={setNotes} multiline />
            </ScrollView>
            <View style={{ flexDirection: "row", gap: space.sm, marginTop: space.lg }}>
              <PrimaryButton title="Cancel" variant="ghost" onPress={() => setOpen(false)} style={{ flex: 1, minWidth: 0 }} />
              <PrimaryButton title="Save" loading={createObligation.isPending} onPress={() => void submit()} style={{ flex: 1, minWidth: 0 }} />
            </View>
          </View>
        </View>
      </Modal>
    </Screen>
  );
}
