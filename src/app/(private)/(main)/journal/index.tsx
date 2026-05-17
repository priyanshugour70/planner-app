import { useCallback, useState } from "react";
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
import { useJournalList, useJournalAnalytics, useJournalMutations } from "@/modules/journal";
import type { JournalEntryDTO } from "@/types/planner";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

const FAB_CLEARANCE = 88;

const MOOD_OPTIONS = [
  { value: "amazing", emoji: "🤩", label: "Amazing" },
  { value: "good", emoji: "😊", label: "Good" },
  { value: "neutral", emoji: "😐", label: "Neutral" },
  { value: "bad", emoji: "😞", label: "Bad" },
  { value: "terrible", emoji: "😢", label: "Terrible" },
] as const;

function moodEmoji(mood: string | null): string {
  return MOOD_OPTIONS.find((m) => m.value === mood)?.emoji ?? "";
}

export default function JournalScreen() {
  const c = usePlannerTheme();
  const list = useJournalList({ limit: 100 });
  const analyticsQuery = useJournalAnalytics();
  const { create, patch, remove, toggleFavorite } = useJournalMutations();

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<JournalEntryDTO | null>(null);
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [mood, setMood] = useState("");
  const [tags, setTags] = useState("");
  const [entryDate, setEntryDate] = useState(new Date().toISOString().slice(0, 10));

  const entries = list.data ?? [];
  const analytics = analyticsQuery.data;

  const openCreate = useCallback(() => {
    setEditing(null); setTitle(""); setBody(""); setMood(""); setTags("");
    setEntryDate(new Date().toISOString().slice(0, 10));
    setModalOpen(true);
  }, []);

  const openEdit = (e: JournalEntryDTO) => {
    setEditing(e); setTitle(e.title); setBody(e.body);
    setMood(e.mood ?? ""); setTags(e.tags.join(", "));
    setEntryDate(e.entryDate ?? new Date().toISOString().slice(0, 10));
    setModalOpen(true);
  };

  const submit = async () => {
    const input: Record<string, unknown> = {
      title: title.trim(), body: body.trim(), entryDate,
    };
    if (mood) input.mood = mood;
    if (tags.trim()) input.tags = tags.split(",").map((t) => t.trim()).filter(Boolean);
    try {
      if (editing) await patch.mutateAsync({ id: editing.id, body: input });
      else await create.mutateAsync(input);
      setModalOpen(false);
    } catch (e) {
      Alert.alert("Error", e instanceof Error ? e.message : "Failed");
    }
  };

  const onDelete = (e: JournalEntryDTO) => {
    Alert.alert("Delete entry?", "This cannot be undone.", [
      { text: "Cancel", style: "cancel" },
      {
        text: "Delete", style: "destructive",
        onPress: () => void remove.mutateAsync(e.id).catch(() => Alert.alert("Failed")),
      },
    ]);
  };

  const onToggleFavorite = (e: JournalEntryDTO) => {
    void toggleFavorite.mutateAsync({ id: e.id, isFavorite: !e.isFavorite });
  };

  const listHeader = (
    <View style={{ marginBottom: space.md }}>
      {/* Hero card */}
      <View style={{
        marginBottom: space.lg, padding: space.lg, borderRadius: radius.lg,
        backgroundColor: c.accentSoft, borderWidth: 1, borderColor: c.accent + "44",
      }}>
        <AppText variant="caption" color="accent" style={{ fontWeight: "600", letterSpacing: 0.5 }}>JOURNAL</AppText>
        <AppText variant="title" style={{ marginTop: space.xs }}>Capture your thoughts</AppText>
        {analytics?.promptOfTheDay ? (
          <AppText variant="caption" color="secondary" style={{ marginTop: space.sm, fontStyle: "italic" }}>
            💡 {analytics.promptOfTheDay}
          </AppText>
        ) : null}
      </View>

      {/* Stats */}
      {analytics ? (
        <View style={{ marginBottom: space.lg }}>
          <SectionHeader title="Writing stats" />
          <View style={{ flexDirection: "row", flexWrap: "wrap", gap: space.sm }}>
            <StatTile label="Entries" value={String(analytics.totalEntries)} tone="accent" />
            <StatTile label="Words written" value={analytics.totalWords.toLocaleString()} tone="income" />
          </View>
          <View style={{ flexDirection: "row", flexWrap: "wrap", gap: space.sm, marginTop: space.sm }}>
            <StatTile label="Writing streak" value={`${analytics.currentWritingStreak}d`} tone="warning" />
            <StatTile label="Favorites" value={String(analytics.favoriteCount)} tone="accent" />
          </View>
        </View>
      ) : null}

      <SectionHeader title="Entries" subtitle={`${entries.length} total`} />
    </View>
  );

  return (
    <Screen showBackLink={false}>
      <FlatList
        data={entries}
        keyExtractor={(item) => item.id}
        refreshing={list.isFetching}
        onRefresh={() => { void list.refetch(); void analyticsQuery.refetch(); }}
        ListHeaderComponent={listHeader}
        contentContainerStyle={{ flexGrow: 1, paddingBottom: FAB_CLEARANCE }}
        ListEmptyComponent={
          list.isPending ? <FinanceListSkeleton rows={4} /> :
            <EmptyState icon="book-outline" title="No entries yet" subtitle="Tap + to write your first entry." />
        }
        renderItem={({ item }) => (
          <ListCard>
            <Pressable onPress={() => openEdit(item)}>
              <View style={{ flexDirection: "row", alignItems: "flex-start", gap: space.sm }}>
                {item.mood ? (
                  <AppText style={{ fontSize: 22, marginTop: 1 }}>{moodEmoji(item.mood)}</AppText>
                ) : null}
                <View style={{ flex: 1, minWidth: 0 }}>
                  <AppText variant="subtitle" numberOfLines={1}>{item.title || "Untitled"}</AppText>
                  <AppText variant="caption" color="secondary" style={{ marginTop: 2 }}>
                    {item.entryDate} · {item.wordCount} words
                  </AppText>
                  <AppText variant="caption" color="muted" numberOfLines={2} style={{ marginTop: space.xs }}>
                    {item.body}
                  </AppText>
                </View>
              </View>
              {item.tags.length > 0 ? (
                <View style={{ flexDirection: "row", flexWrap: "wrap", gap: space.xs, marginTop: space.sm }}>
                  {item.tags.slice(0, 4).map((tag) => (
                    <View key={tag} style={{
                      paddingHorizontal: space.sm, paddingVertical: 2,
                      borderRadius: 999, backgroundColor: c.accentSoft,
                    }}>
                      <AppText variant="caption" color="accent" style={{ fontSize: 10 }}>{tag}</AppText>
                    </View>
                  ))}
                </View>
              ) : null}
            </Pressable>
            <View style={{ flexDirection: "row", gap: space.sm, marginTop: space.sm, alignItems: "center" }}>
              <Pressable onPress={() => onToggleFavorite(item)} hitSlop={8}>
                <Ionicons
                  name={item.isFavorite ? "heart" : "heart-outline"}
                  size={20}
                  color={item.isFavorite ? c.danger : c.textMuted}
                />
              </Pressable>
              <Pressable onPress={() => onDelete(item)} hitSlop={8}>
                <AppText variant="caption" color="danger">Delete</AppText>
              </Pressable>
            </View>
          </ListCard>
        )}
      />

      {/* FAB */}
      <Pressable
        onPress={openCreate}
        accessibilityRole="button"
        accessibilityLabel="New journal entry"
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
            borderTopWidth: 3, borderTopColor: c.accent, maxHeight: "92%",
          }}>
            <AppText variant="title" style={{ marginBottom: space.md }}>
              {editing ? "Edit entry" : "New entry"}
            </AppText>
            <ScrollView keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
              <AppTextField label="Title" value={title} onChangeText={setTitle} placeholder="Today's reflection…" />
              <View style={{ height: space.md }} />
              <AppTextField label="How are you feeling?" value={body} onChangeText={setBody} multiline />
              <View style={{ height: space.md }} />

              {/* Mood picker */}
              <AppText variant="caption" color="secondary" style={{ marginBottom: space.sm }}>Mood</AppText>
              <View style={{ flexDirection: "row", gap: space.sm, flexWrap: "wrap", marginBottom: space.md }}>
                <Pressable
                  onPress={() => setMood("")}
                  style={{
                    paddingVertical: space.sm, paddingHorizontal: space.md, borderRadius: radius.md,
                    borderWidth: 1, borderColor: mood === "" ? c.accent : c.border,
                    backgroundColor: mood === "" ? c.accentSoft : "transparent",
                  }}
                >
                  <AppText variant="caption" color={mood === "" ? "accent" : "secondary"}>None</AppText>
                </Pressable>
                {MOOD_OPTIONS.map((m) => (
                  <Pressable
                    key={m.value} onPress={() => setMood(m.value)}
                    style={{
                      paddingVertical: space.sm, paddingHorizontal: space.md, borderRadius: radius.md,
                      borderWidth: 1, borderColor: mood === m.value ? c.accent : c.border,
                      backgroundColor: mood === m.value ? c.accentSoft : "transparent",
                      flexDirection: "row", alignItems: "center", gap: space.xs,
                    }}
                  >
                    <AppText style={{ fontSize: 16 }}>{m.emoji}</AppText>
                    <AppText variant="caption" color={mood === m.value ? "accent" : "secondary"} style={{ fontWeight: mood === m.value ? "700" : "500" }}>
                      {m.label}
                    </AppText>
                  </Pressable>
                ))}
              </View>

              <AppTextField
                label="Tags (comma-separated)" value={tags}
                onChangeText={setTags} placeholder="work, health, ideas"
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
