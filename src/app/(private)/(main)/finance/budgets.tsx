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
import { formatDisplayDate } from "@/lib/format-display-date";
import { formatInrAmount } from "@/lib/format-inr";
import { useBudgetMutations, useBudgets } from "@/modules/finance/hooks/use-finance-queries";
import { currentMonthRange } from "@/modules/finance/utils/dates";
import type { BudgetDTO } from "@/types/planner";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { useFinanceQuickAddStore } from "@/stores/finance-quick-add.store";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

export default function FinanceBudgetsScreen() {
  const c = usePlannerTheme();
  const list = useBudgets();
  const { create, patch, remove } = useBudgetMutations();
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<BudgetDTO | null>(null);
  const [name, setName] = useState("");
  const [category, setCategory] = useState("");
  const [amountLimit, setAmountLimit] = useState("");
  const [periodStart, setPeriodStart] = useState(currentMonthRange().periodStart);
  const [periodEnd, setPeriodEnd] = useState(currentMonthRange().periodEnd);
  const [notes, setNotes] = useState("");

  const openCreate = () => {
    const m = currentMonthRange();
    setEditing(null);
    setName("");
    setCategory("");
    setAmountLimit("");
    setPeriodStart(m.periodStart);
    setPeriodEnd(m.periodEnd);
    setNotes("");
    setOpen(true);
  };

  const quickPending = useFinanceQuickAddStore((s) => s.pending);
  const quickClear = useFinanceQuickAddStore((s) => s.clear);
  useEffect(() => {
    if (!quickPending || quickPending.target !== "budget") return;
    openCreate();
    quickClear();
  }, [quickPending?.id, quickPending?.target, quickClear]);

  const openEdit = (b: BudgetDTO) => {
    const d = currentMonthRange();
    setEditing(b);
    setName(b.name);
    setCategory(b.category ?? "");
    setAmountLimit(b.amountLimit);
    setPeriodStart((b.periodStart ?? d.periodStart).slice(0, 10));
    setPeriodEnd((b.periodEnd ?? d.periodEnd).slice(0, 10));
    setNotes(b.notes ?? "");
    setOpen(true);
  };

  const submit = async () => {
    const body: Record<string, unknown> = {
      name: name.trim(),
      category: category.trim() || null,
      amountLimit,
      periodStart,
      periodEnd,
      notes: notes.trim() || null,
    };
    try {
      if (editing) await patch.mutateAsync({ id: editing.id, body });
      else await create.mutateAsync(body);
      setOpen(false);
      await list.refetch();
    } catch (e) {
      Alert.alert("Error", e instanceof Error ? e.message : "Failed");
    }
  };

  const onDelete = (b: BudgetDTO) => {
    Alert.alert("Delete budget?", b.name, [
      { text: "Cancel", style: "cancel" },
      {
        text: "Delete",
        style: "destructive",
        onPress: () => void remove.mutateAsync(b.id).then(() => list.refetch()),
      },
    ]);
  };

  const data = list.data ?? [];

  return (
    <Screen showBackLink={false}>
      <View style={{ marginBottom: space.md }}>
        <View style={{ flexDirection: "row", alignItems: "center", justifyContent: "space-between", gap: space.md }}>
          <View style={{ flex: 1, minWidth: 0 }}>
            <AppText variant="subtitle">Budgets</AppText>
            <AppText variant="caption" color="secondary" style={{ marginTop: space.xs }}>
              Set limits for a date range and track spending against them.
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
            <EmptyState icon="pie-chart-outline" title="No budgets yet" subtitle="Create your first budget to cap spending for groceries, travel, or any category." />
          )
        }
        renderItem={({ item }) => (
          <ListCard onPress={() => openEdit(item)} tone="warning">
            <AppText variant="subtitle">{item.name}</AppText>
            <AppText variant="caption" color="secondary" style={{ marginTop: space.xs }}>
              {formatDisplayDate(item.periodStart)} → {formatDisplayDate(item.periodEnd)}
            </AppText>
            <AppText variant="title" color="accent" style={{ marginTop: space.sm }}>
              {formatInrAmount(item.amountLimit)}
            </AppText>
            {item.category ? (
              <AppText variant="caption" color="muted" style={{ marginTop: space.xs }}>
                Label · {item.category}
              </AppText>
            ) : null}
            <Pressable onPress={() => onDelete(item)} style={{ marginTop: space.sm, alignSelf: "flex-start" }} hitSlop={8}>
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
              maxHeight: "90%",
              borderTopWidth: 3,
              borderTopColor: c.warning,
            }}
          >
            <AppText variant="title" style={{ marginBottom: space.md }}>
              {editing ? "Edit budget" : "New budget"}
            </AppText>
            <ScrollView keyboardShouldPersistTaps="handled">
              <AppTextField label="Name" value={name} onChangeText={setName} />
              <View style={{ height: space.md }} />
              <AppTextField label="Category label (optional)" value={category} onChangeText={setCategory} />
              <View style={{ height: space.md }} />
              <AppTextField label="Amount limit" value={amountLimit} onChangeText={setAmountLimit} keyboardType="decimal-pad" />
              <View style={{ height: space.md }} />
              <AppDateField label="Period start" value={periodStart} onChange={setPeriodStart} />
              <View style={{ height: space.md }} />
              <AppDateField label="Period end" value={periodEnd} onChange={setPeriodEnd} />
              <View style={{ height: space.md }} />
              <AppTextField label="Notes" value={notes} onChangeText={setNotes} multiline />
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
