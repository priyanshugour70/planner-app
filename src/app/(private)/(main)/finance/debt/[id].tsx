import { useLocalSearchParams } from "expo-router";
import { useState } from "react";
import { Alert, FlatList, Modal, Pressable, ScrollView, View } from "react-native";
import { PrimaryButton } from "@/components/buttons/primary-button";
import { AppTextField } from "@/components/inputs/app-text-field";
import { EmptyState } from "@/components/ui/empty-state";
import { ListCard } from "@/components/ui/list-card";
import { Screen } from "@/components/ui/screen";
import { SkeletonBlock } from "@/components/ui/skeleton";
import { SectionHeader } from "@/components/ui/section-header";
import { AppText } from "@/components/typography/app-text";
import { formatDisplayDate } from "@/lib/format-display-date";
import { formatInrAmount } from "@/lib/format-inr";
import { useDebtMutations, useDebtPayments, useObligation } from "@/modules/finance/hooks/use-finance-queries";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

export default function DebtObligationDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string | string[] }>();
  const obligationId = String(Array.isArray(id) ? id[0] : id ?? "");
  const c = usePlannerTheme();
  const ob = useObligation(obligationId);
  const payments = useDebtPayments(obligationId);
  const { createPayment, patchObligation } = useDebtMutations();
  const [payOpen, setPayOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [amount, setAmount] = useState("");
  const [note, setNote] = useState("");
  const [status, setStatus] = useState("open");
  const [notes, setNotes] = useState("");

  const o = ob.data;

  const openEdit = () => {
    if (!o) return;
    setStatus(o.status);
    setNotes(o.notes ?? "");
    setEditOpen(true);
  };

  const saveObligation = async () => {
    if (!o) return;
    try {
      await patchObligation.mutateAsync({ id: o.id, body: { status, notes: notes.trim() || null } });
      setEditOpen(false);
      await ob.refetch();
    } catch (e) {
      Alert.alert("Error", e instanceof Error ? e.message : "Failed");
    }
  };

  const recordPayment = async () => {
    try {
      await createPayment.mutateAsync({ obligationId, body: { amount, note: note.trim() || null } });
      setPayOpen(false);
      setAmount("");
      setNote("");
      await Promise.all([payments.refetch(), ob.refetch()]);
    } catch (e) {
      Alert.alert("Error", e instanceof Error ? e.message : "Failed");
    }
  };

  if (ob.isPending) {
    return (
      <Screen showBackLink={false}>
        <View style={{ marginTop: space.md, gap: space.md }}>
          <SkeletonBlock height={120} />
          <SkeletonBlock height={48} />
          <SkeletonBlock height={48} />
          <View style={{ marginTop: space.lg, gap: space.sm }}>
            <SkeletonBlock height={72} />
            <SkeletonBlock height={72} />
            <SkeletonBlock height={72} />
          </View>
        </View>
      </Screen>
    );
  }

  if (ob.isError || !o) {
    return (
      <Screen scroll showBackLink={false}>
        <EmptyState icon="alert-circle-outline" title="Not found" subtitle="This obligation may have been removed. Go back and refresh the list." />
      </Screen>
    );
  }

  const tone = o.direction === "lent" ? "income" : "expense";

  return (
    <Screen scroll showBackLink={false}>
      <ListCard tone={tone}>
        <AppText variant="title" numberOfLines={2}>
          {o.counterparty}
        </AppText>
        <View style={{ flexDirection: "row", alignItems: "center", gap: space.sm, marginTop: space.sm, flexWrap: "wrap" }}>
          <View
            style={{
              paddingHorizontal: space.sm,
              paddingVertical: 2,
              borderRadius: radius.sm,
              backgroundColor: o.direction === "lent" ? c.successSoft : c.dangerSoft,
            }}
          >
            <AppText variant="caption" color={o.direction === "lent" ? "success" : "danger"} style={{ fontWeight: "700", textTransform: "capitalize" }}>
              {o.direction}
            </AppText>
          </View>
          <AppText variant="caption" color="secondary" style={{ textTransform: "capitalize" }}>
            {o.status}
          </AppText>
        </View>
        <AppText variant="title" color="accent" style={{ marginTop: space.md }}>
          Balance {formatInrAmount(o.balance)}
        </AppText>
        <AppText variant="caption" color="muted" style={{ marginTop: space.xs }}>
          Principal {formatInrAmount(o.principal)} · Due {formatDisplayDate(o.dueDate)}
        </AppText>
        {o.notes ? (
          <AppText variant="body" color="secondary" style={{ marginTop: space.md }}>
            {o.notes}
          </AppText>
        ) : null}
      </ListCard>

      <View style={{ marginTop: space.lg, gap: space.sm }}>
        <PrimaryButton title="Record payment" onPress={() => setPayOpen(true)} disabled={o.status === "closed"} />
        <PrimaryButton title="Edit status / notes" variant="ghost" onPress={openEdit} />
      </View>

      <View style={{ marginTop: space.xl }}>
        <SectionHeader title="Payments" subtitle="Newest shown first after refresh" />
      </View>

      <FlatList
        data={payments.data ?? []}
        keyExtractor={(p) => p.id}
        scrollEnabled={false}
        ListEmptyComponent={<EmptyState icon="cash-outline" title="No payments yet" subtitle="Record a payment to reduce the balance." />}
        renderItem={({ item }) => (
          <View
            style={{
              padding: space.md,
              borderRadius: radius.lg,
              borderWidth: 1,
              borderColor: c.border,
              marginBottom: space.sm,
              backgroundColor: c.successSoft,
            }}
          >
            <View style={{ flexDirection: "row", justifyContent: "space-between", alignItems: "center" }}>
              <AppText variant="subtitle" color="success">
                {formatInrAmount(item.amount)}
              </AppText>
              <AppText variant="caption" color="muted">
                {formatDisplayDate(item.paidAt)}
              </AppText>
            </View>
            {item.note ? (
              <AppText variant="caption" color="secondary" style={{ marginTop: space.xs }}>
                {item.note}
              </AppText>
            ) : null}
          </View>
        )}
      />

      <Modal visible={payOpen} animationType="slide" transparent>
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
              Record payment
            </AppText>
            <AppTextField label="Amount" value={amount} onChangeText={setAmount} keyboardType="decimal-pad" />
            <View style={{ height: space.md }} />
            <AppTextField label="Note" value={note} onChangeText={setNote} />
            <View style={{ flexDirection: "row", gap: space.sm, marginTop: space.lg }}>
              <PrimaryButton title="Cancel" variant="ghost" onPress={() => setPayOpen(false)} style={{ flex: 1, minWidth: 0 }} />
              <PrimaryButton title="Save" loading={createPayment.isPending} onPress={() => void recordPayment()} style={{ flex: 1, minWidth: 0 }} />
            </View>
          </View>
        </View>
      </Modal>

      <Modal visible={editOpen} animationType="slide" transparent>
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
              Edit obligation
            </AppText>
            <ScrollView keyboardShouldPersistTaps="handled">
              <AppText variant="caption" color="secondary">
                Status
              </AppText>
              <View style={{ flexDirection: "row", gap: space.sm, marginTop: space.xs, marginBottom: space.md }}>
                {(["open", "closed"] as const).map((s) => (
                  <Pressable
                    key={s}
                    onPress={() => setStatus(s)}
                    style={{
                      paddingHorizontal: space.md,
                      paddingVertical: space.sm,
                      borderRadius: radius.md,
                      borderWidth: 1,
                      borderColor: status === s ? c.accent : c.border,
                      backgroundColor: status === s ? c.accentSoft : "transparent",
                    }}
                  >
                    <AppText variant="body" style={{ textTransform: "capitalize" }}>
                      {s}
                    </AppText>
                  </Pressable>
                ))}
              </View>
              <AppTextField label="Notes" value={notes} onChangeText={setNotes} multiline />
            </ScrollView>
            <View style={{ flexDirection: "row", gap: space.sm, marginTop: space.lg }}>
              <PrimaryButton title="Cancel" variant="ghost" onPress={() => setEditOpen(false)} style={{ flex: 1, minWidth: 0 }} />
              <PrimaryButton title="Save" loading={patchObligation.isPending} onPress={() => void saveObligation()} style={{ flex: 1, minWidth: 0 }} />
            </View>
          </View>
        </View>
      </Modal>
    </Screen>
  );
}
