import { useEffect, useState, useRef } from "react";
import { View, TextInput, ActivityIndicator, KeyboardAvoidingView, Platform, Keyboard } from "react-native";
import { useLocalSearchParams, useRouter, Stack } from "expo-router";
import { useNotes, useUpdateNote } from "@/modules/notes/hooks/use-notes-queries";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { space } from "@/theme/spacing/tokens";

export default function NoteEditorScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const router = useRouter();
  const c = usePlannerTheme();
  const { data: notes, isLoading } = useNotes();
  const updateNote = useUpdateNote();

  const note = notes?.find((n) => n.id === id);

  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");

  const titleRef = useRef("");
  const bodyRef = useRef("");
  const saveTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // Load initial data
  useEffect(() => {
    if (note) {
      setTitle(note.title || "");
      setBody(note.body || "");
      titleRef.current = note.title || "";
      bodyRef.current = note.body || "";
    }
  }, [note?.id]);

  // Autosave logic
  useEffect(() => {
    if (!note) return;

    // Don't save if nothing changed from DB state
    if (title === titleRef.current && body === bodyRef.current) return;

    if (saveTimeoutRef.current) {
      clearTimeout(saveTimeoutRef.current);
    }

    saveTimeoutRef.current = setTimeout(() => {
      updateNote.mutate({
        id,
        data: { title, body },
      });
      titleRef.current = title;
      bodyRef.current = body;
    }, 2000); // Save after 2 seconds of inactivity

    return () => {
      if (saveTimeoutRef.current) clearTimeout(saveTimeoutRef.current);
    };
  }, [title, body, id, note]);

  // Save immediately on unmount if there are unsaved changes
  useEffect(() => {
    return () => {
      if (saveTimeoutRef.current) {
        clearTimeout(saveTimeoutRef.current);
        if (titleRef.current !== title || bodyRef.current !== body) {
          updateNote.mutate({
            id,
            data: { title, body },
          });
        }
      }
    };
  }, [title, body, id]);

  if (isLoading) {
    return (
      <View style={{ flex: 1, backgroundColor: c.surface, alignItems: "center", justifyContent: "center" }}>
        <ActivityIndicator color={c.accent} />
      </View>
    );
  }

  if (!note) {
    return null; // or a not-found UI
  }

  return (
    <KeyboardAvoidingView
      style={{ flex: 1, backgroundColor: c.surface }}
      behavior={Platform.OS === "ios" ? "padding" : undefined}
      keyboardVerticalOffset={Platform.OS === "ios" ? 88 : 0}
    >
      <Stack.Screen
        options={{
          title: title || "Untitled",
          headerTitleStyle: { color: c.textSecondary, fontSize: 14 },
        }}
      />
      <View style={{ flex: 1, padding: space.lg }}>
        <TextInput
          style={{
            fontSize: 24,
            fontWeight: "700",
            color: c.textPrimary,
            marginBottom: space.md,
          }}
          placeholder="Note Title"
          placeholderTextColor={c.textMuted}
          value={title}
          onChangeText={setTitle}
          returnKeyType="next"
        />
        <TextInput
          style={{
            flex: 1,
            fontSize: 16,
            color: c.textPrimary,
            textAlignVertical: "top",
            lineHeight: 24,
          }}
          placeholder="Start typing..."
          placeholderTextColor={c.textMuted}
          value={body}
          onChangeText={setBody}
          multiline
        />
      </View>
    </KeyboardAvoidingView>
  );
}
