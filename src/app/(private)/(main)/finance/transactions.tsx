import { useEffect, useMemo, useState } from "react";
import { Alert, FlatList, Modal, Pressable, ScrollView, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { PrimaryButton } from "@/components/buttons/primary-button";
import { AppDateField } from "@/components/inputs/app-date-field";
import { AppTextField } from "@/components/inputs/app-text-field";
import { EmptyState } from "@/components/ui/empty-state";
import { ListCard, type ListCardTone } from "@/components/ui/list-card";
import { PillButton } from "@/components/ui/pill-button";
import { Screen } from "@/components/ui/screen";
import { FinanceListSkeleton } from "@/components/ui/skeleton";
import { AppText } from "@/components/typography/app-text";
import { formatDisplayDate } from "@/lib/format-display-date";
import { formatInrAmount } from "@/lib/format-inr";
import {
  useAccounts,
  useBudgets,
  useCategories,
  useTransactionMutations,
  useTransactionsList,
} from "@/modules/finance/hooks/use-finance-queries";
import { localMonthRange, previousLocalMonthRange } from "@/modules/finance/utils/date-bounds";
import { todayISO } from "@/modules/finance/utils/dates";
import type { TransactionDTO } from "@/types/planner";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { useFinanceQuickAddStore } from "@/stores/finance-quick-add.store";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

type TxFilter = "all" | "this_month" | "last_month" | "custom";

function parseTagsCsv(raw: string): string[] | undefined {
  const t = raw
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean);
  return t.length ? t : undefined;
}

function rowTone(kind: string): ListCardTone {
  return kind === "income" ? "income" : "expense";
}

export default function FinanceTransactionsScreen() {
  const c = usePlannerTheme();
  const [txFilter, setTxFilter] = useState<TxFilter>("this_month");
  const [customFrom, setCustomFrom] = useState(todayISO());
  const [customTo, setCustomTo] = useState(todayISO());

  const listParams = useMemo(() => {
    const limit = 100;
    if (txFilter === "all") return { limit };
    if (txFilter === "this_month") {
      const { from, to } = localMonthRange();
      return { from, to, limit };
    }
    if (txFilter === "last_month") {
      const { from, to } = previousLocalMonthRange();
      return { from, to, limit };
    }
    if (customFrom <= customTo) return { from: customFrom, to: customTo, limit };
    return { from: customTo, to: customFrom, limit };
  }, [txFilter, customFrom, customTo]);

  const list = useTransactionsList(listParams);
  const budgets = useBudgets();
  const accounts = useAccounts();
  const categories = useCategories();
  const { create, patch, remove } = useTransactionMutations();

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<TransactionDTO | null>(null);
  const [kind, setKind] = useState<"income" | "expense">("expense");
  const [amount, setAmount] = useState("");
  const [note, setNote] = useState("");
  const [occurredOn, setOccurredOn] = useState(todayISO());
  const [budgetId, setBudgetId] = useState<string | null>(null);
  const [accountId, setAccountId] = useState<string | null>(null);
  const [categoryId, setCategoryId] = useState<string | null>(null);
  const [categoryFreeText, setCategoryFreeText] = useState("");
  const [merchant, setMerchant] = useState("");
  const [paymentMethod, setPaymentMethod] = useState("");
  const [tagsCsv, setTagsCsv] = useState("");
  const [picker, setPicker] = useState<"budget" | "account" | "category" | null>(null);

  const rows = list.data?.rows ?? [];

  const openCreate = () => {
    setEditing(null);
    setKind("expense");
    setAmount("");
    setNote("");
    setOccurredOn(todayISO());
    setBudgetId(null);
    setAccountId(null);
    setCategoryId(null);
    setCategoryFreeText("");
    setMerchant("");
    setPaymentMethod("");
    setTagsCsv("");
    setModalOpen(true);
  };

  const quickPending = useFinanceQuickAddStore((s) => s.pending);
  const quickClear = useFinanceQuickAddStore((s) => s.clear);
  useEffect(() => {
    if (!quickPending || quickPending.target !== "transaction") return;
    openCreate();
    quickClear();
  }, [quickPending?.id, quickPending?.target, quickClear]);

  const openEdit = (t: TransactionDTO) => {
    setEditing(t);
    setKind(t.kind === "income" ? "income" : "expense");
    setAmount(String(t.amount));
    setNote(t.note ?? "");
    setOccurredOn(t.occurredOn ?? todayISO());
    setBudgetId(t.budgetId);
    setAccountId(t.accountId ?? null);
    setCategoryId(t.categoryId ?? null);
    setCategoryFreeText(t.category ?? "");
    setMerchant(t.merchant ?? "");
    setPaymentMethod(t.paymentMethod ?? "");
    setTagsCsv((t.tags ?? []).join(", "));
    setModalOpen(true);
  };

  const submit = async () => {
    const tags = parseTagsCsv(tagsCsv);
    const body: Record<string, unknown> = {
      kind,
      amount,
      note: note.trim() || null,
      occurredOn,
      budgetId,
      accountId,
      categoryId,
      category: categoryFreeText.trim() || null,
      merchant: merchant.trim() || null,
      paymentMethod: paymentMethod.trim() || null,
    };
    if (tags) body.tags = tags;
    try {
      if (editing) await patch.mutateAsync({ id: editing.id, body });
      else await create.mutateAsync(body);
      setModalOpen(false);
      await list.refetch();
    } catch (e) {
      Alert.alert("Error", e instanceof Error ? e.message : "Failed");
    }
  };

  const onDelete = (t: TransactionDTO) => {
    Alert.alert("Delete transaction?", undefined, [
      { text: "Cancel", style: "cancel" },
      {
        text: "Delete",
        style: "destructive",
        onPress: () =>
          void remove.mutateAsync(t.id).then(() => list.refetch()).catch(() => Alert.alert("Failed")),
      },
    ]);
  };

  const pickerItems = useMemo(() => {
    if (picker === "budget") return budgets.data?.map((b) => ({ id: b.id, label: b.name })) ?? [];
    if (picker === "account") return accounts.data?.map((a) => ({ id: a.id, label: `${a.name} (${a.kind})` })) ?? [];
    if (picker === "category") return categories.data?.map((x) => ({ id: x.id, label: `${x.name} (${x.kind})` })) ?? [];
    return [];
  }, [picker, budgets.data, accounts.data, categories.data]);

  const filterChip = (key: TxFilter, label: string) => {
    const active = txFilter === key;
    return (
      <Pressable
        key={key}
        onPress={() => setTxFilter(key)}
        style={{
          alignSelf: "flex-start",
          paddingVertical: 6,
          paddingHorizontal: space.md,
          borderRadius: 999,
          borderWidth: 1,
          borderColor: active ? c.accent : c.border,
          backgroundColor: active ? c.accentSoft : c.surfaceElevated,
        }}
      >
        <AppText variant="caption" color={active ? "accent" : "secondary"} style={{ fontWeight: active ? "700" : "500" }}>
          {label}
        </AppText>
      </Pressable>
    );
  };

  return (
    <Screen showBackLink={false}>
      <View style={{ marginBottom: space.sm }}>
        <View style={{ flexDirection: "row", alignItems: "center", justifyContent: "space-between", gap: space.md }}>
          <View style={{ flex: 1, minWidth: 0 }}>
            <AppText variant="subtitle">Transactions</AppText>
            <AppText variant="caption" color="secondary" style={{ marginTop: 4 }}>
              Tap a row to edit, or add a new entry.
            </AppText>
          </View>
          <PillButton label="Add" onPress={openCreate} icon={<Ionicons name="add" size={20} color="#0B0B10" />} />
        </View>
      </View>

      <View style={{ marginBottom: space.sm }}>
        <AppText variant="caption" color="secondary" style={{ marginBottom: space.xs }}>
          Date range
        </AppText>
        <View style={{ flexDirection: "row", flexWrap: "wrap", gap: space.sm, alignItems: "center" }}>
          {filterChip("this_month", "This month")}
          {filterChip("last_month", "Last month")}
          {filterChip("all", "All (latest)")}
          {filterChip("custom", "Custom")}
        </View>
      </View>
      {txFilter === "custom" ? (
        <View style={{ flexDirection: "row", gap: space.sm, marginBottom: space.md }}>
          <View style={{ flex: 1 }}>
            <AppDateField label="From" value={customFrom} onChange={setCustomFrom} />
          </View>
          <View style={{ flex: 1 }}>
            <AppDateField label="To" value={customTo} onChange={setCustomTo} />
          </View>
        </View>
      ) : null}

      <FlatList
        style={{ flex: 1 }}
        data={rows}
        keyExtractor={(item) => item.id}
        refreshing={list.isFetching}
        onRefresh={() => list.refetch()}
        ListEmptyComponent={
          list.isPending ? (
            <FinanceListSkeleton rows={5} />
          ) : (
            <EmptyState
              icon="receipt-outline"
              title="No transactions in this range"
              subtitle="Change the filter above or add a new entry."
            />
          )
        }
        renderItem={({ item }) => (
          <ListCard onPress={() => openEdit(item)} tone={rowTone(item.kind)}>
            <View style={{ flexDirection: "row", justifyContent: "space-between", alignItems: "center", gap: space.sm }}>
              <View style={{ flexDirection: "row", alignItems: "center", gap: space.sm, flex: 1, minWidth: 0 }}>
                <View
                  style={{
                    paddingHorizontal: space.sm,
                    paddingVertical: 4,
                    borderRadius: radius.sm,
                    backgroundColor: item.kind === "income" ? c.successSoft : c.dangerSoft,
                  }}
                >
                  <AppText variant="caption" color={item.kind === "income" ? "success" : "danger"} style={{ fontWeight: "700", textTransform: "capitalize" }}>
                    {item.kind}
                  </AppText>
                </View>
                <AppText variant="subtitle" numberOfLines={1} style={{ flex: 1 }}>
                  {item.note?.trim() ? item.note : "No note"}
                </AppText>
              </View>
              <AppText variant="subtitle" color={item.kind === "income" ? "success" : "danger"}>
                {item.kind === "expense" ? "−" : "+"}
                {formatInrAmount(item.amount)}
              </AppText>
            </View>
            <AppText variant="caption" color="muted" style={{ marginTop: space.xs }}>
              {formatDisplayDate(item.occurredOn)}
            </AppText>
            <Pressable onPress={() => onDelete(item)} style={{ marginTop: space.sm, alignSelf: "flex-start" }} hitSlop={8}>
              <AppText variant="caption" color="danger">
                Delete
              </AppText>
            </Pressable>
          </ListCard>
        )}
      />

      <Modal visible={modalOpen} animationType="slide" transparent>
        <View style={{ flex: 1, backgroundColor: c.overlay, justifyContent: "flex-end" }}>
          <View
            style={{
              backgroundColor: c.surface,
              padding: space.lg,
              borderTopLeftRadius: radius.xl,
              borderTopRightRadius: radius.xl,
              maxHeight: "92%",
              borderTopWidth: 3,
              borderTopColor: c.accent,
            }}
          >
            <AppText variant="title" style={{ marginBottom: space.md }}>
              {editing ? "Edit transaction" : "New transaction"}
            </AppText>
            <ScrollView keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
              <AppText variant="caption" color="secondary">
                Type
              </AppText>
              <View style={{ flexDirection: "row", gap: space.sm, marginTop: space.xs, marginBottom: space.md }}>
                <Pressable
                  onPress={() => setKind("expense")}
                  style={{
                    flex: 1,
                    padding: space.sm,
                    borderRadius: radius.md,
                    borderWidth: 1,
                    borderColor: kind === "expense" ? c.danger : c.border,
                    backgroundColor: kind === "expense" ? c.dangerSoft : "transparent",
                  }}
                >
                  <AppText variant="body" style={{ textAlign: "center" }} color={kind === "expense" ? "danger" : "primary"}>
                    Expense
                  </AppText>
                </Pressable>
                <Pressable
                  onPress={() => setKind("income")}
                  style={{
                    flex: 1,
                    padding: space.sm,
                    borderRadius: radius.md,
                    borderWidth: 1,
                    borderColor: kind === "income" ? c.success : c.border,
                    backgroundColor: kind === "income" ? c.successSoft : "transparent",
                  }}
                >
                  <AppText variant="body" style={{ textAlign: "center" }} color={kind === "income" ? "success" : "primary"}>
                    Income
                  </AppText>
                </Pressable>
              </View>
              <AppTextField label="Amount" value={amount} onChangeText={setAmount} keyboardType="decimal-pad" />
              <View style={{ height: space.md }} />
              <AppDateField label="Date" value={occurredOn} onChange={setOccurredOn} />
              <View style={{ height: space.md }} />
              <AppTextField label="Note" value={note} onChangeText={setNote} />
              <View style={{ height: space.md }} />
              <Pressable
                onPress={() => setPicker("budget")}
                style={{ paddingVertical: space.sm, borderRadius: radius.md, backgroundColor: c.inputBg, paddingHorizontal: space.md }}
              >
                <AppText variant="caption" color="secondary">
                  Budget (optional)
                </AppText>
                <AppText variant="body">{budgetId ? budgets.data?.find((b) => b.id === budgetId)?.name ?? budgetId : "None"}</AppText>
              </Pressable>
              <View style={{ height: space.sm }} />
              <Pressable
                onPress={() => setPicker("account")}
                style={{ paddingVertical: space.sm, borderRadius: radius.md, backgroundColor: c.inputBg, paddingHorizontal: space.md }}
              >
                <AppText variant="caption" color="secondary">
                  Account (optional)
                </AppText>
                <AppText variant="body">{accountId ? accounts.data?.find((a) => a.id === accountId)?.name ?? accountId : "None"}</AppText>
              </Pressable>
              <View style={{ height: space.sm }} />
              <Pressable
                onPress={() => setPicker("category")}
                style={{ paddingVertical: space.sm, borderRadius: radius.md, backgroundColor: c.inputBg, paddingHorizontal: space.md }}
              >
                <AppText variant="caption" color="secondary">
                  Category (optional)
                </AppText>
                <AppText variant="body">{categoryId ? categories.data?.find((x) => x.id === categoryId)?.name ?? categoryId : "None"}</AppText>
              </Pressable>
              <View style={{ height: space.md }} />
              <AppTextField
                label="Category label (optional)"
                value={categoryFreeText}
                onChangeText={setCategoryFreeText}
                placeholder="Free-text label on the receipt"
              />
              <View style={{ height: space.sm }} />
              <AppTextField label="Merchant (optional)" value={merchant} onChangeText={setMerchant} />
              <View style={{ height: space.sm }} />
              <AppTextField label="Payment method (optional)" value={paymentMethod} onChangeText={setPaymentMethod} />
              <View style={{ height: space.sm }} />
              <AppTextField
                label="Tags (optional, comma-separated)"
                value={tagsCsv}
                onChangeText={setTagsCsv}
                placeholder="groceries, weekend"
              />
            </ScrollView>
            <View style={{ flexDirection: "row", gap: space.sm, marginTop: space.lg }}>
              <PrimaryButton title="Cancel" variant="ghost" onPress={() => setModalOpen(false)} style={{ flex: 1, minWidth: 0 }} />
              <PrimaryButton title="Save" loading={create.isPending || patch.isPending} onPress={() => void submit()} style={{ flex: 1, minWidth: 0 }} />
            </View>
          </View>
        </View>
      </Modal>

      <Modal visible={picker !== null} animationType="fade" transparent>
        <Pressable style={{ flex: 1, backgroundColor: c.overlay }} onPress={() => setPicker(null)}>
          <View
            style={{
              marginTop: "auto",
              backgroundColor: c.surface,
              maxHeight: "50%",
              borderTopLeftRadius: radius.xl,
              borderTopRightRadius: radius.xl,
              borderTopWidth: 2,
              borderTopColor: c.accent,
            }}
          >
            <AppText variant="subtitle" style={{ padding: space.lg, paddingBottom: space.sm }}>
              Choose
            </AppText>
            <FlatList
              data={[{ id: "__none__", label: "None" }, ...pickerItems]}
              keyExtractor={(x) => x.id}
              renderItem={({ item }) => (
                <Pressable
                  onPress={() => {
                    if (picker === "budget") setBudgetId(item.id === "__none__" ? null : item.id);
                    if (picker === "account") setAccountId(item.id === "__none__" ? null : item.id);
                    if (picker === "category") setCategoryId(item.id === "__none__" ? null : item.id);
                    setPicker(null);
                  }}
                  style={{ padding: space.lg, borderBottomWidth: 1, borderBottomColor: c.border }}
                >
                  <AppText variant="body">{item.label}</AppText>
                </Pressable>
              )}
            />
          </View>
        </Pressable>
      </Modal>
    </Screen>
  );
}
