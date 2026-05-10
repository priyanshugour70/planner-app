import { useEffect, useState } from "react";
import { Alert, FlatList, Modal, Pressable, ScrollView, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { PrimaryButton } from "@/components/buttons/primary-button";
import { AppDateField } from "@/components/inputs/app-date-field";
import { AppTextField } from "@/components/inputs/app-text-field";
import { EmptyState } from "@/components/ui/empty-state";
import { ListCard } from "@/components/ui/list-card";
import { PillButton } from "@/components/ui/pill-button";
import { FinanceListSkeleton } from "@/components/ui/skeleton";
import { Screen } from "@/components/ui/screen";
import { AppText } from "@/components/typography/app-text";
import { formatInrAmount } from "@/lib/format-inr";
import { useRecurringMutations, useRecurringRules } from "@/modules/finance/hooks/use-finance-queries";
import { formatDisplayDate } from "@/lib/format-display-date";
import { todayISO } from "@/modules/finance/utils/dates";
import type { RecurringRuleDTO } from "@/types/planner";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { useFinanceQuickAddStore } from "@/stores/finance-quick-add.store";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

export default function FinanceRecurringScreen() {
  const c = usePlannerTheme();
  const list = useRecurringRules();
  const { create, patch, remove, materializeDue } = useRecurringMutations();
  const [open, setOpen] = useState(false);
  const [label, setLabel] = useState("");
  const [templateKind, setTemplateKind] = useState<"expense" | "income">("expense");
  const [templateAmount, setTemplateAmount] = useState("");
  const [templateCategory, setTemplateCategory] = useState("");
  const [nextRunOn, setNextRunOn] = useState(todayISO());

  const openCreate = () => {
    setLabel("");
    setTemplateKind("expense");
    setTemplateAmount("");
    setTemplateCategory("");
    setNextRunOn(todayISO());
    setOpen(true);
  };

  const quickPending = useFinanceQuickAddStore((s) => s.pending);
  const quickClear = useFinanceQuickAddStore((s) => s.clear);
  useEffect(() => {
    if (!quickPending || quickPending.target !== "recurring") return;
    openCreate();
    quickClear();
  }, [quickPending?.id, quickPending?.target, quickClear]);

  const submit = async () => {
    const body: Record<string, unknown> = {
      label: label.trim(),
      templateKind,
      templateAmount,
      templateCategory: templateCategory.trim() || null,
      cadence: "monthly",
      nextRunOn,
      active: true,
    };
    try {
      await create.mutateAsync(body);
      setOpen(false);
      await list.refetch();
    } catch (e) {
      Alert.alert("Error", e instanceof Error ? e.message : "Failed");
    }
  };

  const onMaterialize = async () => {
    try {
      const res = await materializeDue.mutateAsync({});
      const n = res.createdTransactionIds?.length ?? 0;
      Alert.alert("Done", n ? `Created ${n} transaction(s).` : "No due rules to materialize.");
      await list.refetch();
    } catch (e) {
      Alert.alert("Error", e instanceof Error ? e.message : "Failed");
    }
  };

  const rows = list.data ?? [];

  return (
    <Screen showBackLink={false}>
      <View style={{ marginBottom: space.md }}>
        <View style={{ flexDirection: "row", alignItems: "flex-start", justifyContent: "space-between", gap: space.md }}>
          <View style={{ flex: 1, minWidth: 0 }}>
            <AppText variant="subtitle">EMI & recurring</AppText>
            <AppText variant="caption" color="secondary" style={{ marginTop: space.xs }}>
              Monthly templates become real transactions when you run “Materialize due” (through today, UTC).
            </AppText>
          </View>
          <PillButton label="Add" onPress={openCreate} icon={<Ionicons name="add" size={20} color="#0B0B10" />} />
        </View>
      </View>

      <PrimaryButton
        title={materializeDue.isPending ? "Running…" : "Materialize due (through today)"}
        loading={materializeDue.isPending}
        onPress={() => void onMaterialize()}
      />
      <View style={{ height: space.md }} />

      <FlatList
        style={{ flex: 1 }}
        data={rows}
        keyExtractor={(x) => x.id}
        refreshing={list.isFetching}
        onRefresh={() => list.refetch()}
        ListEmptyComponent={
          list.isPending ? (
            <FinanceListSkeleton rows={4} />
          ) : (
            <EmptyState
              icon="repeat-outline"
              title="No recurring rules"
              subtitle="Add your home loan EMI, rent, or salary. Then materialize to post transactions up to today."
            />
          )
        }
        renderItem={({ item }: { item: RecurringRuleDTO }) => (
          <ListCard tone={item.templateKind === "income" ? "income" : "expense"}>
            <View style={{ flexDirection: "row", justifyContent: "space-between", alignItems: "flex-start", gap: space.md }}>
              <View style={{ flex: 1, minWidth: 0 }}>
                <AppText variant="subtitle">{item.label}</AppText>
                <AppText variant="caption" color="secondary" style={{ marginTop: space.xs, textTransform: "capitalize" }}>
                  {item.templateKind} · {item.cadence}
                </AppText>
                <AppText variant="title" color="accent" style={{ marginTop: space.sm }}>
                  {formatInrAmount(item.templateAmount)}
                </AppText>
                <AppText variant="caption" color="muted" style={{ marginTop: space.xs }}>
                  Next run {formatDisplayDate(item.nextRunOn)}
                  {item.active ? "" : " · paused"}
                </AppText>
              </View>
              <Pressable
                onPress={() =>
                  void patch
                    .mutateAsync({ id: item.id, body: { active: !item.active } })
                    .then(() => list.refetch())
                    .catch(() => Alert.alert("Could not update"))
                }
                style={{ padding: space.sm }}
              >
                <Ionicons name={item.active ? "pause-circle-outline" : "play-circle-outline"} size={28} color={c.accent} />
              </Pressable>
            </View>
            <Pressable
              onPress={() =>
                Alert.alert("Delete rule?", item.label, [
                  { text: "Cancel", style: "cancel" },
                  { text: "Delete", style: "destructive", onPress: () => void remove.mutateAsync(item.id).then(() => list.refetch()) },
                ])
              }
              style={{ marginTop: space.sm, alignSelf: "flex-start" }}
            >
              <AppText variant="caption" color="danger">
                Delete
              </AppText>
            </Pressable>
          </ListCard>
        )}
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
              New recurring / EMI
            </AppText>
            <ScrollView keyboardShouldPersistTaps="handled">
              <AppTextField label="Label (e.g. Home loan EMI)" value={label} onChangeText={setLabel} />
              <View style={{ height: space.md }} />
              <AppText variant="caption" color="secondary">
                Type
              </AppText>
              <View style={{ flexDirection: "row", gap: space.sm, marginTop: space.xs, marginBottom: space.md }}>
                {(["expense", "income"] as const).map((k) => (
                  <Pressable
                    key={k}
                    onPress={() => setTemplateKind(k)}
                    style={{
                      flex: 1,
                      padding: space.sm,
                      borderRadius: radius.md,
                      borderWidth: 1,
                      borderColor: templateKind === k ? (k === "income" ? c.success : c.danger) : c.border,
                      backgroundColor: templateKind === k ? (k === "income" ? c.successSoft : c.dangerSoft) : "transparent",
                    }}
                  >
                    <AppText variant="body" style={{ textAlign: "center", textTransform: "capitalize" }} color={templateKind === k ? (k === "income" ? "success" : "danger") : "primary"}>
                      {k}
                    </AppText>
                  </Pressable>
                ))}
              </View>
              <AppTextField label="Amount" value={templateAmount} onChangeText={setTemplateAmount} keyboardType="decimal-pad" />
              <View style={{ height: space.md }} />
              <AppTextField label="Category label (optional)" value={templateCategory} onChangeText={setTemplateCategory} />
              <View style={{ height: space.md }} />
              <AppDateField label="First run date" value={nextRunOn} onChange={setNextRunOn} />
            </ScrollView>
            <View style={{ flexDirection: "row", gap: space.sm, marginTop: space.lg }}>
              <PrimaryButton title="Cancel" variant="ghost" onPress={() => setOpen(false)} style={{ flex: 1, minWidth: 0 }} />
              <PrimaryButton title="Save" loading={create.isPending} onPress={() => void submit()} style={{ flex: 1, minWidth: 0 }} />
            </View>
          </View>
        </View>
      </Modal>
    </Screen>
  );
}
