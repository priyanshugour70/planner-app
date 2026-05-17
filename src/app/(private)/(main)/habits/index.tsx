import { useCallback, useEffect, useState } from "react";
import { Alert, FlatList, Modal, Pressable, ScrollView, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { PrimaryButton } from "@/components/buttons/primary-button";
import { AppTextField } from "@/components/inputs/app-text-field";
import { EmptyState } from "@/components/ui/empty-state";
import { ListCard } from "@/components/ui/list-card";
import { Screen } from "@/components/ui/screen";
import { FinanceListSkeleton } from "@/components/ui/skeleton";
import { StatTile } from "@/components/ui/stat-tile";
import { SectionHeader } from "@/components/ui/section-header";
import { AppText } from "@/components/typography/app-text";
import { useHabitsList, useHabitsAnalytics, useHabitMutations } from "@/modules/habits";
import type { HabitDTO } from "@/types/planner";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

const FAB_CLEARANCE = 88;

const FREQ_OPTIONS = ["daily", "weekly", "custom"] as const;
const COLOR_PRESETS = ["#6366f1", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6", "#06b6d4"];
const ICON_PRESETS = ["🎯", "💪", "📚", "🧘", "🏃", "💧", "🍎", "😴", "✍️", "🧹"];

export default function HabitsScreen() {
  const c = usePlannerTheme();
  const list = useHabitsList({ archived: false });
  const analyticsQuery = useHabitsAnalytics();
  const { create, patch, remove, logEntry } = useHabitMutations();

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<HabitDTO | null>(null);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [color, setColor] = useState("#6366f1");
  const [icon, setIcon] = useState("🎯");
  const [frequency, setFrequency] = useState<"daily" | "weekly" | "custom">("daily");
  const [targetPerWeek, setTargetPerWeek] = useState("7");

  const habits = list.data ?? [];
  const summary = analyticsQuery.data?.summary;
  const analyticsMap = Object.fromEntries(
    (analyticsQuery.data?.habits ?? []).map((a) => [a.habitId, a])
  );

  const todayStr = new Date().toISOString().slice(0, 10);

  const openCreate = useCallback(() => {
    setEditing(null); setName(""); setDescription("");
    setColor("#6366f1"); setIcon("🎯"); setFrequency("daily"); setTargetPerWeek("7");
    setModalOpen(true);
  }, []);

  const openEdit = (h: HabitDTO) => {
    setEditing(h); setName(h.name); setDescription(h.description ?? "");
    setColor(h.color); setIcon(h.icon); setFrequency(h.frequency as "daily" | "weekly" | "custom");
    setTargetPerWeek(String(h.targetPerWeek ?? 7));
    setModalOpen(true);
  };

  const submit = async () => {
    const body: Record<string, unknown> = {
      name: name.trim(), description: description.trim() || null,
      color, icon, frequency, targetPerWeek: Number(targetPerWeek) || 7,
    };
    try {
      if (editing) await patch.mutateAsync({ id: editing.id, body });
      else await create.mutateAsync(body);
      setModalOpen(false);
    } catch (e) {
      Alert.alert("Error", e instanceof Error ? e.message : "Failed");
    }
  };

  const onLogToday = async (h: HabitDTO) => {
    try {
      await logEntry.mutateAsync({ habitId: h.id, body: { entryDate: todayStr, count: 1 } });
    } catch {
      Alert.alert("Could not log habit");
    }
  };

  const onDelete = (h: HabitDTO) => {
    Alert.alert("Delete habit?", "All history will be deleted.", [
      { text: "Cancel", style: "cancel" },
      {
        text: "Delete", style: "destructive",
        onPress: () => void remove.mutateAsync(h.id).catch(() => Alert.alert("Failed")),
      },
    ]);
  };

  const onArchive = (h: HabitDTO) => {
    void patch.mutateAsync({ id: h.id, body: { archived: true } }).catch(() => Alert.alert("Failed"));
  };

  const listHeader = (
    <View style={{ marginBottom: space.md }}>
      {/* Hero card */}
      <View style={{
        marginBottom: space.lg, padding: space.lg, borderRadius: radius.lg,
        backgroundColor: c.accentSoft, borderWidth: 1, borderColor: c.accent + "44",
      }}>
        <AppText variant="caption" color="accent" style={{ fontWeight: "600", letterSpacing: 0.5 }}>HABITS</AppText>
        <AppText variant="title" style={{ marginTop: space.xs }}>Build your streaks</AppText>
        <AppText variant="body" color="secondary" style={{ marginTop: space.sm }}>
          Log daily progress, track streaks, and build lasting habits.
        </AppText>
      </View>

      {/* Stats */}
      {summary ? (
        <View style={{ marginBottom: space.lg }}>
          <SectionHeader title="Today's snapshot" />
          <View style={{ flexDirection: "row", flexWrap: "wrap", gap: space.sm }}>
            <StatTile label="Active habits" value={String(summary.activeHabits)} tone="accent" />
            <StatTile label="Logged today" value={String(summary.todayLogged)} tone="income" />
          </View>
          <View style={{ flexDirection: "row", flexWrap: "wrap", gap: space.sm, marginTop: space.sm }}>
            <StatTile label="Active streaks" value={String(summary.totalStreaksActive)} tone="warning" />
            <StatTile label="Best streak" value={`${summary.bestCurrentStreak}d`} tone="accent" />
          </View>
        </View>
      ) : null}

      <SectionHeader title="Your habits" subtitle={`${habits.length} active`} />
    </View>
  );

  return (
    <Screen showBackLink={false}>
      <FlatList
        data={habits}
        keyExtractor={(item) => item.id}
        refreshing={list.isFetching}
        onRefresh={() => { void list.refetch(); void analyticsQuery.refetch(); }}
        ListHeaderComponent={listHeader}
        contentContainerStyle={{ flexGrow: 1, paddingBottom: FAB_CLEARANCE }}
        ListEmptyComponent={
          list.isPending ? <FinanceListSkeleton rows={4} /> :
            <EmptyState icon="checkmark-circle-outline" title="No habits yet" subtitle="Tap + to create your first habit." />
        }
        renderItem={({ item }) => {
          const a = analyticsMap[item.id];
          return (
            <ListCard>
              <Pressable onPress={() => openEdit(item)}>
                <View style={{ flexDirection: "row", alignItems: "center", gap: space.md }}>
                  <View style={{
                    width: 40, height: 40, borderRadius: radius.md,
                    alignItems: "center", justifyContent: "center",
                    backgroundColor: item.color + "22",
                  }}>
                    <AppText style={{ fontSize: 20 }}>{item.icon}</AppText>
                  </View>
                  <View style={{ flex: 1, minWidth: 0 }}>
                    <AppText variant="subtitle" numberOfLines={1}>{item.name}</AppText>
                    <AppText variant="caption" color="secondary" style={{ marginTop: 2 }}>
                      {item.frequency}
                      {a && a.currentStreak > 0 ? `  🔥 ${a.currentStreak}d streak` : ""}
                    </AppText>
                  </View>
                </View>
              </Pressable>
              <View style={{ flexDirection: "row", gap: space.sm, marginTop: space.sm, alignItems: "center" }}>
                <Pressable
                  onPress={() => void onLogToday(item)}
                  hitSlop={8}
                  style={{
                    paddingVertical: space.sm, paddingHorizontal: space.md,
                    borderRadius: radius.md, backgroundColor: c.accent + "22",
                    borderWidth: 1, borderColor: c.accent + "55",
                  }}
                >
                  <AppText variant="caption" color="accent" style={{ fontWeight: "700" }}>Log today</AppText>
                </Pressable>
                <Pressable onPress={() => onArchive(item)} hitSlop={8}>
                  <AppText variant="caption" color="secondary">Archive</AppText>
                </Pressable>
                <Pressable onPress={() => onDelete(item)} hitSlop={8}>
                  <AppText variant="caption" color="danger">Delete</AppText>
                </Pressable>
              </View>
            </ListCard>
          );
        }}
      />

      {/* FAB */}
      <Pressable
        onPress={openCreate}
        accessibilityRole="button"
        accessibilityLabel="New habit"
        style={{
          position: "absolute", bottom: space.xl, right: space.lg,
          width: 56, height: 56, borderRadius: 28,
          backgroundColor: c.accent, alignItems: "center", justifyContent: "center",
          shadowColor: "#000", shadowOpacity: 0.25, shadowRadius: 8, shadowOffset: { width: 0, height: 4 },
          elevation: 6,
        }}
      >
        <Ionicons name="add" size={28} color="#fff" />
      </Pressable>

      {/* Create / Edit modal */}
      <Modal visible={modalOpen} animationType="slide" transparent onRequestClose={() => setModalOpen(false)}>
        <View style={{ flex: 1, backgroundColor: c.overlay, justifyContent: "flex-end" }}>
          <View style={{
            backgroundColor: c.surface, padding: space.lg,
            borderTopLeftRadius: radius.xl, borderTopRightRadius: radius.xl,
            borderTopWidth: 3, borderTopColor: c.accent, maxHeight: "90%",
          }}>
            <AppText variant="title" style={{ marginBottom: space.md }}>
              {editing ? "Edit habit" : "New habit"}
            </AppText>
            <ScrollView keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
              <AppTextField label="Name" value={name} onChangeText={setName} />
              <View style={{ height: space.md }} />
              <AppTextField label="Description (optional)" value={description} onChangeText={setDescription} />
              <View style={{ height: space.md }} />

              {/* Icon picker */}
              <AppText variant="caption" color="secondary" style={{ marginBottom: space.sm }}>Icon</AppText>
              <View style={{ flexDirection: "row", flexWrap: "wrap", gap: space.sm, marginBottom: space.md }}>
                {ICON_PRESETS.map((ic) => (
                  <Pressable
                    key={ic} onPress={() => setIcon(ic)}
                    style={{
                      width: 44, height: 44, borderRadius: radius.md, alignItems: "center", justifyContent: "center",
                      borderWidth: 2, borderColor: icon === ic ? c.accent : c.border,
                      backgroundColor: icon === ic ? c.accentSoft : c.inputBg,
                    }}
                  >
                    <AppText style={{ fontSize: 20 }}>{ic}</AppText>
                  </Pressable>
                ))}
              </View>

              {/* Color picker */}
              <AppText variant="caption" color="secondary" style={{ marginBottom: space.sm }}>Color</AppText>
              <View style={{ flexDirection: "row", gap: space.sm, marginBottom: space.md }}>
                {COLOR_PRESETS.map((col) => (
                  <Pressable
                    key={col} onPress={() => setColor(col)}
                    style={{
                      width: 32, height: 32, borderRadius: 16, backgroundColor: col,
                      borderWidth: color === col ? 3 : 1,
                      borderColor: color === col ? c.textPrimary : "transparent",
                    }}
                  />
                ))}
              </View>

              {/* Frequency */}
              <AppText variant="caption" color="secondary" style={{ marginBottom: space.sm }}>Frequency</AppText>
              <View style={{ flexDirection: "row", gap: space.sm, marginBottom: space.md }}>
                {FREQ_OPTIONS.map((f) => (
                  <Pressable
                    key={f} onPress={() => setFrequency(f)}
                    style={{
                      paddingVertical: space.sm, paddingHorizontal: space.md,
                      borderRadius: radius.md, borderWidth: 1,
                      borderColor: frequency === f ? c.accent : c.border,
                      backgroundColor: frequency === f ? c.accentSoft : "transparent",
                    }}
                  >
                    <AppText variant="caption" color={frequency === f ? "accent" : "secondary"}
                      style={{ textTransform: "capitalize", fontWeight: frequency === f ? "700" : "500" }}>
                      {f}
                    </AppText>
                  </Pressable>
                ))}
              </View>

              <AppTextField
                label="Target / week" value={targetPerWeek}
                onChangeText={setTargetPerWeek} keyboardType="number-pad"
              />
            </ScrollView>
            <View style={{ flexDirection: "row", gap: space.sm, marginTop: space.lg }}>
              <PrimaryButton title="Cancel" variant="ghost" onPress={() => setModalOpen(false)} style={{ flex: 1 }} />
              <PrimaryButton
                title="Save" loading={create.isPending || patch.isPending}
                onPress={() => void submit()} style={{ flex: 1 }}
              />
            </View>
          </View>
        </View>
      </Modal>
    </Screen>
  );
}
