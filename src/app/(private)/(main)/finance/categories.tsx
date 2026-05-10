import { useEffect, useMemo, useState } from "react";
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
import { useCategories, useCategoryMutations } from "@/modules/finance/hooks/use-finance-queries";
import type { FinanceCategoryDTO } from "@/types/planner";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { useFinanceQuickAddStore } from "@/stores/finance-quick-add.store";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

export default function FinanceCategoriesScreen() {
  const c = usePlannerTheme();
  const list = useCategories();
  const { create, patch, remove } = useCategoryMutations();
  const [open, setOpen] = useState(false);
  const [picker, setPicker] = useState(false);
  const [editing, setEditing] = useState<FinanceCategoryDTO | null>(null);
  const [name, setName] = useState("");
  const [kind, setKind] = useState("expense");
  const [parentId, setParentId] = useState<string | null>(null);

  const parents = useMemo(() => list.data?.filter((x) => !editing || x.id !== editing.id) ?? [], [list.data, editing]);

  const openCreate = () => {
    setEditing(null);
    setName("");
    setKind("expense");
    setParentId(null);
    setOpen(true);
  };

  const quickPending = useFinanceQuickAddStore((s) => s.pending);
  const quickClear = useFinanceQuickAddStore((s) => s.clear);
  useEffect(() => {
    if (!quickPending || quickPending.target !== "categories") return;
    openCreate();
    quickClear();
  }, [quickPending?.id, quickPending?.target, quickClear]);

  const openEdit = (x: FinanceCategoryDTO) => {
    setEditing(x);
    setName(x.name);
    setKind(x.kind);
    setParentId(x.parentId);
    setOpen(true);
  };

  const submit = async () => {
    const body: Record<string, unknown> = { name: name.trim(), kind: kind.trim(), parentId };
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
            <AppText variant="subtitle">Categories</AppText>
            <AppText variant="caption" color="secondary" style={{ marginTop: space.xs }}>
              Organize spending and income; optional parent for sub-categories.
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
            <EmptyState icon="pricetags-outline" title="No categories" subtitle="Create categories like Rent, Salary, or Dining to tag transactions." />
          )
        }
        renderItem={({ item }) => {
          const tone = item.kind === "income" ? "income" : "expense";
          const parentName = item.parentId ? data.find((p) => p.id === item.parentId)?.name : null;
          return (
            <ListCard onPress={() => openEdit(item)} tone={tone}>
              <View style={{ flexDirection: "row", alignItems: "center", gap: space.sm, flexWrap: "wrap" }}>
                <AppText variant="subtitle">{item.name}</AppText>
                <View
                  style={{
                    paddingHorizontal: space.sm,
                    paddingVertical: 2,
                    borderRadius: radius.sm,
                    backgroundColor: item.kind === "income" ? c.successSoft : c.dangerSoft,
                  }}
                >
                  <AppText variant="caption" color={item.kind === "income" ? "success" : "danger"} style={{ fontWeight: "700", textTransform: "capitalize" }}>
                    {item.kind}
                  </AppText>
                </View>
              </View>
              {parentName ? (
                <AppText variant="caption" color="muted" style={{ marginTop: space.xs }}>
                  Under · {parentName}
                </AppText>
              ) : null}
              <Pressable
                onPress={() =>
                  Alert.alert("Delete category?", item.name, [
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
              maxHeight: "88%",
              borderTopWidth: 3,
              borderTopColor: c.accent,
            }}
          >
            <AppText variant="title" style={{ marginBottom: space.md }}>
              {editing ? "Edit category" : "New category"}
            </AppText>
            <ScrollView keyboardShouldPersistTaps="handled">
              <AppTextField label="Name" value={name} onChangeText={setName} />
              <View style={{ height: space.md }} />
              <AppText variant="caption" color="secondary">
                Kind
              </AppText>
              <View style={{ flexDirection: "row", gap: space.sm, marginTop: space.xs }}>
                {(["expense", "income"] as const).map((k) => (
                  <Pressable
                    key={k}
                    onPress={() => setKind(k)}
                    style={{
                      paddingHorizontal: space.md,
                      paddingVertical: space.sm,
                      borderRadius: radius.md,
                      borderWidth: 1,
                      borderColor: kind === k ? (k === "income" ? c.success : c.danger) : c.border,
                      backgroundColor: kind === k ? (k === "income" ? c.successSoft : c.dangerSoft) : "transparent",
                    }}
                  >
                    <AppText variant="body" color={kind === k ? (k === "income" ? "success" : "danger") : "primary"} style={{ textTransform: "capitalize" }}>
                      {k}
                    </AppText>
                  </Pressable>
                ))}
              </View>
              <Pressable
                onPress={() => setPicker(true)}
                style={{ marginTop: space.lg, padding: space.md, borderRadius: radius.md, backgroundColor: c.inputBg }}
              >
                <AppText variant="caption" color="secondary">
                  Parent (optional)
                </AppText>
                <AppText variant="body">{parentId ? list.data?.find((p) => p.id === parentId)?.name ?? parentId : "None"}</AppText>
              </Pressable>
            </ScrollView>
            <View style={{ flexDirection: "row", gap: space.sm, marginTop: space.lg }}>
              <PrimaryButton title="Cancel" variant="ghost" onPress={() => setOpen(false)} style={{ flex: 1, minWidth: 0 }} />
              <PrimaryButton title="Save" loading={create.isPending || patch.isPending} onPress={() => void submit()} style={{ flex: 1, minWidth: 0 }} />
            </View>
          </View>
        </View>
      </Modal>

      <Modal visible={picker} transparent animationType="fade">
        <Pressable style={{ flex: 1, backgroundColor: c.overlay }} onPress={() => setPicker(false)}>
          <View
            style={{
              marginTop: "auto",
              backgroundColor: c.surface,
              maxHeight: "45%",
              borderTopLeftRadius: radius.xl,
              borderTopRightRadius: radius.xl,
              borderTopWidth: 2,
              borderTopColor: c.accent,
            }}
          >
            <AppText variant="subtitle" style={{ padding: space.lg, paddingBottom: space.sm }}>
              Parent category
            </AppText>
            <FlatList
              data={[{ id: "__none__", name: "None" }, ...parents.map((p) => ({ id: p.id, name: p.name }))]}
              keyExtractor={(x) => x.id}
              renderItem={({ item }) => (
                <Pressable
                  onPress={() => {
                    setParentId(item.id === "__none__" ? null : item.id);
                    setPicker(false);
                  }}
                  style={{ padding: space.lg, borderBottomWidth: 1, borderBottomColor: c.border }}
                >
                  <AppText variant="body">{item.name}</AppText>
                </Pressable>
              )}
            />
          </View>
        </Pressable>
      </Modal>
    </Screen>
  );
}
