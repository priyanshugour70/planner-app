import { useCallback, useEffect, useMemo, useState } from "react";
import { Alert, FlatList, Modal, Pressable, ScrollView, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { PrimaryButton } from "@/components/buttons/primary-button";
import { AppDateField } from "@/components/inputs/app-date-field";
import { AppTextField } from "@/components/inputs/app-text-field";
import { EmptyState } from "@/components/ui/empty-state";
import { ListCard } from "@/components/ui/list-card";
import { Screen } from "@/components/ui/screen";
import { FinanceListSkeleton } from "@/components/ui/skeleton";
import { AppText } from "@/components/typography/app-text";
import { formatDisplayDate } from "@/lib/format-display-date";
import { useGoalsList } from "@/modules/goals/hooks/use-goals-queries";
import { useTaskMutations, useTasksList } from "@/modules/tasks/hooks/use-tasks-queries";
import { todayISO } from "@/modules/finance/utils/dates";
import type { GoalDTO, TaskDTO } from "@/types/planner";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { useTasksQuickAddStore } from "@/stores/tasks-quick-add.store";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

const FAB_CLEARANCE = 88;

type StatusFilter = "" | "todo" | "in_progress" | "done" | "cancelled";
type SortKey = "due" | "updated" | "priority" | "created";

function parseTags(raw: string): string[] | undefined {
  const t = raw
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean);
  return t.length ? t : undefined;
}

function filterSummary(status: StatusFilter, roots: boolean, q: string): string {
  const parts: string[] = [];
  if (status) parts.push(status.replace("_", " "));
  if (roots) parts.push("roots");
  if (q.trim()) parts.push(`“${q.trim().slice(0, 18)}${q.trim().length > 18 ? "…" : ""}”`);
  return parts.length ? parts.join(" · ") : "Defaults";
}

export default function TasksScreen() {
  const c = usePlannerTheme();
  const openCreateSignal = useTasksQuickAddStore((s) => s.openCreateSignal);

  const [filtersExpanded, setFiltersExpanded] = useState(false);
  const [statusFilter, setStatusFilter] = useState<StatusFilter>("");
  const [rootsOnly, setRootsOnly] = useState(false);
  const [sort, setSort] = useState<SortKey>("due");
  const [search, setSearch] = useState("");

  const listParams = useMemo(
    () => ({
      status: statusFilter || undefined,
      rootsOnly: rootsOnly || undefined,
      sort,
      q: search.trim() || undefined,
      limit: 200,
    }),
    [statusFilter, rootsOnly, sort, search]
  );

  const list = useTasksList(listParams);
  const goals = useGoalsList();
  const { create, patch, remove } = useTaskMutations();

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<TaskDTO | null>(null);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState("2");
  const [dueOpt, setDueOpt] = useState<string | null>(null);
  const [goalId, setGoalId] = useState<string | null>(null);
  const [parentTaskId, setParentTaskId] = useState<string | null>(null);
  const [tagsCsv, setTagsCsv] = useState("");
  const [status, setStatus] = useState<"todo" | "in_progress" | "done" | "cancelled">("todo");
  const [picker, setPicker] = useState<"goal" | "parent" | null>(null);

  const rows = list.data ?? [];

  const openCreate = useCallback(() => {
    setEditing(null);
    setTitle("");
    setDescription("");
    setPriority("2");
    setDueOpt(null);
    setGoalId(null);
    setParentTaskId(null);
    setTagsCsv("");
    setStatus("todo");
    setModalOpen(true);
  }, []);

  useEffect(() => {
    if (openCreateSignal === 0) return;
    openCreate();
  }, [openCreateSignal, openCreate]);

  const openEdit = (t: TaskDTO) => {
    setEditing(t);
    setTitle(t.title);
    setDescription(t.description ?? "");
    setPriority(String(t.priority));
    setDueOpt(t.dueAt && t.dueAt.length >= 10 ? t.dueAt.slice(0, 10) : null);
    setGoalId(t.goalId);
    setParentTaskId(t.parentTaskId);
    setTagsCsv((t.tags ?? []).join(", "));
    setStatus((t.status as typeof status) ?? "todo");
    setModalOpen(true);
  };

  const submit = async () => {
    const tags = parseTags(tagsCsv);
    const body: Record<string, unknown> = {
      title: title.trim(),
      description: description.trim() || null,
      status,
      priority: Number(priority) || 2,
      dueAt: dueOpt,
      goalId,
      parentTaskId,
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

  const onToggleDone = async (t: TaskDTO) => {
    const next = t.status === "done" ? "todo" : "done";
    try {
      await patch.mutateAsync({ id: t.id, body: { status: next } });
      await list.refetch();
    } catch {
      Alert.alert("Could not update");
    }
  };

  const onDelete = (t: TaskDTO) => {
    Alert.alert("Delete task?", "Sub-tasks are removed with it.", [
      { text: "Cancel", style: "cancel" },
      {
        text: "Delete",
        style: "destructive",
        onPress: () =>
          void remove
            .mutateAsync(t.id)
            .then(() => list.refetch())
            .catch(() => Alert.alert("Failed")),
      },
    ]);
  };

  const pickerItems: { id: string; label: string }[] = useMemo(() => {
    if (picker === "goal") {
      return [{ id: "__none__", label: "None" }, ...(goals.data ?? []).map((g: GoalDTO) => ({ id: g.id, label: g.title }))];
    }
    if (picker === "parent") {
      return [
        { id: "__none__", label: "None" },
        ...rows.filter((x) => !editing || x.id !== editing.id).map((x) => ({ id: x.id, label: x.title })),
      ];
    }
    return [];
  }, [picker, goals.data, rows, editing]);

  const chip = (key: StatusFilter, label: string) => {
    const active = statusFilter === key;
    return (
      <Pressable
        key={key || "all"}
        onPress={() => setStatusFilter(key)}
        style={{
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

  const listHeader = (
    <View style={{ marginBottom: space.md }}>
      <Pressable
        onPress={() => setFiltersExpanded((v) => !v)}
        accessibilityRole="button"
        accessibilityState={{ expanded: filtersExpanded }}
        style={{
          flexDirection: "row",
          alignItems: "center",
          justifyContent: "space-between",
          paddingVertical: space.md,
          paddingHorizontal: space.md,
          borderRadius: radius.lg,
          backgroundColor: c.card,
          borderWidth: 1,
          borderColor: c.border,
        }}
      >
        <View style={{ flex: 1, minWidth: 0, paddingRight: space.sm }}>
          <AppText variant="subtitle">Filters & search</AppText>
          <AppText variant="caption" color="muted" style={{ marginTop: 4 }} numberOfLines={1}>
            {filterSummary(statusFilter, rootsOnly, search)}
          </AppText>
        </View>
        <Ionicons name={filtersExpanded ? "chevron-up" : "chevron-down"} size={22} color={c.textMuted} />
      </Pressable>

      {filtersExpanded ? (
        <View
          style={{
            marginTop: space.sm,
            padding: space.md,
            borderRadius: radius.lg,
            backgroundColor: c.surfaceElevated,
            borderWidth: 1,
            borderColor: c.border,
            gap: space.md,
          }}
        >
          <View>
            <AppText variant="caption" color="secondary">
              Status
            </AppText>
            <View style={{ flexDirection: "row", flexWrap: "wrap", gap: space.sm, marginTop: space.xs }}>
              {chip("", "All")}
              {chip("todo", "Todo")}
              {chip("in_progress", "Doing")}
              {chip("done", "Done")}
              {chip("cancelled", "Cancelled")}
            </View>
          </View>

          <View style={{ flexDirection: "row", flexWrap: "wrap", gap: space.sm, alignItems: "center" }}>
            <Pressable
              onPress={() => setRootsOnly((v) => !v)}
              style={{
                paddingVertical: 6,
                paddingHorizontal: space.md,
                borderRadius: 999,
                borderWidth: 1,
                borderColor: rootsOnly ? c.accent : c.border,
                backgroundColor: rootsOnly ? c.accentSoft : c.surfaceElevated,
              }}
            >
              <AppText variant="caption" color={rootsOnly ? "accent" : "secondary"} style={{ fontWeight: rootsOnly ? "700" : "500" }}>
                Roots only
              </AppText>
            </Pressable>
            {(["due", "priority", "updated"] as const).map((s) => {
              const active = sort === s;
              return (
                <Pressable
                  key={s}
                  onPress={() => setSort(s)}
                  style={{
                    paddingVertical: 6,
                    paddingHorizontal: space.md,
                    borderRadius: 999,
                    borderWidth: 1,
                    borderColor: active ? c.accent : c.border,
                    backgroundColor: active ? c.accentSoft : c.surfaceElevated,
                  }}
                >
                  <AppText variant="caption" color={active ? "accent" : "secondary"} style={{ fontWeight: active ? "700" : "500" }}>
                    {s === "due" ? "By due" : s === "priority" ? "Priority" : "Updated"}
                  </AppText>
                </Pressable>
              );
            })}
          </View>

          <AppTextField label="Search title" value={search} onChangeText={setSearch} placeholder="Substring in title" />
          <PrimaryButton title="Apply search" variant="ghost" onPress={() => void list.refetch()} />
        </View>
      ) : null}
    </View>
  );

  return (
    <Screen showBackLink={false}>
      <FlatList
        data={rows}
        keyExtractor={(item) => item.id}
        refreshing={list.isFetching}
        onRefresh={() => list.refetch()}
        ListHeaderComponent={listHeader}
        contentContainerStyle={{ flexGrow: 1, paddingBottom: FAB_CLEARANCE }}
        ListEmptyComponent={
          list.isPending ? (
            <FinanceListSkeleton rows={5} />
          ) : (
            <EmptyState icon="checkbox-outline" title="No tasks" subtitle="Tap + to add one, or widen filters." />
          )
        }
        renderItem={({ item }) => (
          <ListCard>
            <Pressable onPress={() => openEdit(item)}>
              <AppText variant="subtitle" numberOfLines={2}>
                {item.title}
              </AppText>
              <AppText variant="caption" color="secondary" style={{ marginTop: space.xs, textTransform: "capitalize" }}>
                {item.status.replace("_", " ")} · P{item.priority}
                {item.dueAt ? ` · Due ${formatDisplayDate(item.dueAt)}` : ""}
              </AppText>
            </Pressable>
            <View style={{ flexDirection: "row", marginTop: space.sm, gap: space.md, alignItems: "center" }}>
              <Pressable onPress={() => void onToggleDone(item)} hitSlop={8}>
                <Ionicons name={item.status === "done" ? "checkbox" : "square-outline"} size={24} color={c.accent} />
              </Pressable>
              <Pressable onPress={() => onDelete(item)} hitSlop={8}>
                <AppText variant="caption" color="danger">
                  Delete
                </AppText>
              </Pressable>
            </View>
          </ListCard>
        )}
      />

      <Modal visible={modalOpen} animationType="slide" transparent onRequestClose={() => setModalOpen(false)}>
        <View style={{ flex: 1, backgroundColor: c.overlay, justifyContent: "flex-end" }}>
          <View
            style={{
              backgroundColor: c.surface,
              padding: space.lg,
              borderTopLeftRadius: radius.xl,
              borderTopRightRadius: radius.xl,
              borderTopWidth: 3,
              borderTopColor: c.accent,
              maxHeight: "92%",
            }}
          >
            <AppText variant="title" style={{ marginBottom: space.md }}>
              {editing ? "Edit task" : "New task"}
            </AppText>
            <ScrollView keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
              <AppTextField label="Title" value={title} onChangeText={setTitle} />
              <View style={{ height: space.md }} />
              <AppTextField label="Description" value={description} onChangeText={setDescription} />
              <View style={{ height: space.md }} />
              <AppText variant="caption" color="secondary">
                Status
              </AppText>
              <View style={{ flexDirection: "row", flexWrap: "wrap", gap: space.sm, marginTop: space.xs, marginBottom: space.md }}>
                {(["todo", "in_progress", "done", "cancelled"] as const).map((s) => (
                  <Pressable
                    key={s}
                    onPress={() => setStatus(s)}
                    style={{
                      paddingVertical: space.sm,
                      paddingHorizontal: space.md,
                      borderRadius: radius.md,
                      borderWidth: 1,
                      borderColor: status === s ? c.accent : c.border,
                      backgroundColor: status === s ? c.accentSoft : "transparent",
                    }}
                  >
                    <AppText variant="caption" color={status === s ? "accent" : "secondary"} style={{ textTransform: "capitalize" }}>
                      {s.replace("_", " ")}
                    </AppText>
                  </Pressable>
                ))}
              </View>
              <AppTextField label="Priority (0–5)" value={priority} onChangeText={setPriority} keyboardType="number-pad" />
              <View style={{ height: space.md }} />
              {dueOpt == null ? (
                <Pressable
                  onPress={() => setDueOpt(todayISO())}
                  style={{ paddingVertical: space.sm, borderRadius: radius.md, backgroundColor: c.inputBg, paddingHorizontal: space.md }}
                >
                  <AppText variant="caption" color="secondary">
                    Due date
                  </AppText>
                  <AppText variant="body">Tap to add</AppText>
                </Pressable>
              ) : (
                <View>
                  <AppDateField label="Due date" value={dueOpt} onChange={setDueOpt} />
                  <Pressable onPress={() => setDueOpt(null)} style={{ marginTop: space.xs }}>
                    <AppText variant="caption" color="danger">
                      Remove due date
                    </AppText>
                  </Pressable>
                </View>
              )}
              <View style={{ height: space.md }} />
              <Pressable
                onPress={() => setPicker("goal")}
                style={{ paddingVertical: space.sm, borderRadius: radius.md, backgroundColor: c.inputBg, paddingHorizontal: space.md }}
              >
                <AppText variant="caption" color="secondary">
                  Goal
                </AppText>
                <AppText variant="body">{goalId ? goals.data?.find((g) => g.id === goalId)?.title ?? goalId : "None"}</AppText>
              </Pressable>
              <View style={{ height: space.sm }} />
              <Pressable
                onPress={() => setPicker("parent")}
                style={{ paddingVertical: space.sm, borderRadius: radius.md, backgroundColor: c.inputBg, paddingHorizontal: space.md }}
              >
                <AppText variant="caption" color="secondary">
                  Parent task
                </AppText>
                <AppText variant="body">{parentTaskId ? rows.find((x) => x.id === parentTaskId)?.title ?? parentTaskId : "None"}</AppText>
              </Pressable>
              <View style={{ height: space.sm }} />
              <AppTextField label="Tags (comma-separated)" value={tagsCsv} onChangeText={setTagsCsv} />
            </ScrollView>
            <View style={{ flexDirection: "row", gap: space.sm, marginTop: space.lg }}>
              <PrimaryButton title="Cancel" variant="ghost" onPress={() => setModalOpen(false)} style={{ flex: 1, minWidth: 0 }} />
              <PrimaryButton
                title="Save"
                loading={create.isPending || patch.isPending}
                onPress={() => void submit()}
                style={{ flex: 1, minWidth: 0 }}
              />
            </View>
          </View>
        </View>
      </Modal>

      <Modal visible={picker !== null} animationType="fade" transparent onRequestClose={() => setPicker(null)}>
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
              data={pickerItems}
              keyExtractor={(x) => x.id}
              renderItem={({ item }) => (
                <Pressable
                  onPress={() => {
                    if (picker === "goal") setGoalId(item.id === "__none__" ? null : item.id);
                    if (picker === "parent") setParentTaskId(item.id === "__none__" ? null : item.id);
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
