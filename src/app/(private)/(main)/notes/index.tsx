import { ActivityIndicator, Pressable, View } from "react-native";
import { useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { href } from "@/navigation/href";
import { Screen } from "@/components/ui/screen";
import { AppText } from "@/components/typography/app-text";
import { useNotes, useCreateNote, useDeleteNote } from "@/modules/notes/hooks/use-notes-queries";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { space } from "@/theme/spacing/tokens";
import { radius } from "@/theme/radius/tokens";

export default function NotesScreen() {
  const router = useRouter();
  const c = usePlannerTheme();
  const { data: notes, isLoading } = useNotes();
  const createNote = useCreateNote();
  const deleteNote = useDeleteNote();

  const handleCreate = async () => {
    const newNote = await createNote.mutateAsync({ title: "Untitled", body: "" });
    router.push(href(`/(private)/(main)/notes/${newNote.id}`));
  };

  if (isLoading) {
    return (
      <Screen style={{ justifyContent: "center", alignItems: "center" }}>
        <ActivityIndicator color={c.accent} />
      </Screen>
    );
  }

  return (
    <Screen scroll>
      <View style={{ gap: space.md, paddingBottom: 100 }}>
        {notes?.length === 0 && (
          <View style={{ alignItems: "center", justifyContent: "center", paddingVertical: 40 }}>
            <Ionicons name="document-text-outline" size={48} color={c.textMuted} />
            <AppText color="muted" style={{ marginTop: space.sm }}>
              No notes yet. Tap the + to create one.
            </AppText>
          </View>
        )}

        {notes?.map((note) => (
          <Pressable
            key={note.id}
            onPress={() => router.push(href(`/(private)/(main)/notes/${note.id}`))}
            style={({ pressed }) => ({
              backgroundColor: pressed ? c.surfaceElevated : c.card,
              padding: space.lg,
              borderRadius: radius.lg,
              borderWidth: 1,
              borderColor: c.border,
              flexDirection: "row",
              alignItems: "center",
            })}
          >
            <View style={{ flex: 1, gap: 4 }}>
              <AppText variant="subtitle" numberOfLines={1}>
                {note.title || "Untitled"}
              </AppText>
              <AppText variant="caption" color="secondary" numberOfLines={2}>
                {note.body || "No additional text"}
              </AppText>
            </View>
            <Pressable
              hitSlop={10}
              onPress={() => deleteNote.mutate(note.id)}
              style={{ padding: 8 }}
            >
              <Ionicons name="trash-outline" size={20} color={c.danger} />
            </Pressable>
          </Pressable>
        ))}
      </View>

      {/* FAB */}
      <View
        style={{
          position: "absolute",
          bottom: space.xl,
          right: space.lg,
        }}
      >
        <Pressable
          onPress={handleCreate}
          style={({ pressed }) => ({
            width: 56,
            height: 56,
            borderRadius: 28,
            backgroundColor: c.accent,
            alignItems: "center",
            justifyContent: "center",
            shadowColor: "#000",
            shadowOffset: { width: 0, height: 4 },
            shadowOpacity: 0.3,
            shadowRadius: 4,
            elevation: 5,
            opacity: pressed ? 0.8 : 1,
          })}
        >
          <Ionicons name="add" size={30} color="#fff" />
        </Pressable>
      </View>
    </Screen>
  );
}
