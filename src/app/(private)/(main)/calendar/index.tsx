import { useState, useMemo } from "react";
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  View,
  Modal,
  TextInput,
  Alert
} from "react-native";
import { useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { Screen } from "@/components/ui/screen";
import { AppText } from "@/components/typography/app-text";
import {
  useCalendarDailySummary,
  useCreateCalendarEvent,
  useDeleteCalendarEvent
} from "@/modules/calendar/hooks/use-calendar-queries";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { space } from "@/theme/spacing/tokens";
import { radius } from "@/theme/radius/tokens";
import { href } from "@/navigation/href";

export default function CalendarScreen() {
  const router = useRouter();
  const c = usePlannerTheme();

  const [selectedMonth, setSelectedMonth] = useState(() => new Date().toISOString().slice(0, 7)); // YYYY-MM
  const [selectedDateStr, setSelectedDateStr] = useState<string | null>(
    () => new Date().toISOString().slice(0, 10)
  );

  // Event modal states
  const [modalVisible, setModalVisible] = useState(false);
  const [title, setTitle] = useState("");
  const [starts, setStarts] = useState("");
  const [ends, setEnds] = useState("");

  const { data: summary, isLoading, refetch } = useCalendarDailySummary(selectedMonth);
  const createEvent = useCreateCalendarEvent();
  const deleteEvent = useDeleteCalendarEvent();

  // Navigation handlers
  const handlePrevMonth = () => {
    const [year, month] = selectedMonth.split("-").map(Number);
    const prevDate = new Date(year, month - 2, 1);
    setSelectedMonth(prevDate.toISOString().slice(0, 7));
  };

  const handleNextMonth = () => {
    const [year, month] = selectedMonth.split("-").map(Number);
    const nextDate = new Date(year, month, 1);
    setSelectedMonth(nextDate.toISOString().slice(0, 7));
  };

  // Month label e.g. "May 2026"
  const monthLabel = useMemo(() => {
    const [year, month] = selectedMonth.split("-").map(Number);
    return new Date(year, month - 1, 1).toLocaleDateString("en-US", {
      month: "long",
      year: "numeric"
    });
  }, [selectedMonth]);

  // Generate 42 calendar grid days
  const calendarDays = useMemo(() => {
    const [year, month] = selectedMonth.split("-").map(Number);
    const firstDay = new Date(year, month - 1, 1);
    const lastDay = new Date(year, month, 0);

    const days = [];

    // Prev month padding
    const startDayOfWeek = firstDay.getDay(); // 0 is Sunday
    for (let i = startDayOfWeek - 1; i >= 0; i--) {
      const prevDate = new Date(year, month - 1, -i);
      days.push({ date: prevDate, isCurrentMonth: false });
    }

    // Current month days
    const totalDays = lastDay.getDate();
    for (let i = 1; i <= totalDays; i++) {
      const currDate = new Date(year, month - 1, i);
      days.push({ date: currDate, isCurrentMonth: true });
    }

    // Next month padding
    const remaining = 42 - days.length;
    for (let i = 1; i <= remaining; i++) {
      const nextDate = new Date(year, month, i);
      days.push({ date: nextDate, isCurrentMonth: false });
    }

    return days;
  }, [selectedMonth]);

  // Selected date details
  const activeDayDetails = useMemo(() => {
    if (!selectedDateStr || !summary) return null;
    return summary[selectedDateStr] || { tasks: [], habits: [], journals: [], notes: [], transactions: [], events: [] };
  }, [selectedDateStr, summary]);

  const handleAddEvent = async () => {
    if (!title.trim() || !starts || !ends) {
      Alert.alert("Error", "Please fill in all event details.");
      return;
    }
    const startsAt = new Date(`${selectedDateStr}T${starts}:00`).toISOString();
    const endsAt = new Date(`${selectedDateStr}T${ends}:00`).toISOString();

    if (new Date(endsAt) < new Date(startsAt)) {
      Alert.alert("Error", "End time must be after start time.");
      return;
    }

    try {
      await createEvent.mutateAsync({
        title: title.trim(),
        startsAt,
        endsAt
      });
      setTitle("");
      setStarts("");
      setEnds("");
      setModalVisible(false);
      void refetch();
    } catch (e: any) {
      Alert.alert("Error", e.message || "Failed to create event");
    }
  };

  const handleDeleteEvent = (id: string) => {
    Alert.alert("Delete Event", "Are you sure you want to delete this event?", [
      { text: "Cancel", style: "cancel" },
      {
        text: "Delete",
        style: "destructive",
        onPress: async () => {
          try {
            await deleteEvent.mutateAsync(id);
            void refetch();
          } catch (e: any) {
            Alert.alert("Error", e.message || "Failed to delete event");
          }
        }
      }
    ]);
  };

  const weekdays = ["S", "M", "T", "W", "T", "F", "S"];

  return (
    <Screen>
      <ScrollView contentContainerStyle={{ paddingBottom: 120 }}>
        {/* Month Picker Header */}
        <View style={[styles.header, { backgroundColor: c.surfaceElevated, borderColor: c.border }]}>
          <Pressable onPress={handlePrevMonth} style={styles.headerBtn}>
            <Ionicons name="chevron-back" size={20} color={c.textPrimary} />
          </Pressable>
          <AppText variant="subtitle" style={{ fontWeight: "700" }}>
            {monthLabel}
          </AppText>
          <Pressable onPress={handleNextMonth} style={styles.headerBtn}>
            <Ionicons name="chevron-forward" size={20} color={c.textPrimary} />
          </Pressable>
        </View>

        {/* Weekday labels */}
        <View style={styles.weekdaysRow}>
          {weekdays.map((day, idx) => (
            <AppText key={idx} variant="caption" color="muted" style={styles.weekdayText}>
              {day}
            </AppText>
          ))}
        </View>

        {/* Month grid */}
        {isLoading ? (
          <View style={{ height: 320, justifyContent: "center", alignItems: "center" }}>
            <ActivityIndicator color={c.accent} size="large" />
          </View>
        ) : (
          <View style={[styles.grid, { borderColor: c.border }]}>
            {calendarDays.map(({ date, isCurrentMonth }, idx) => {
              const dateStr = date.toISOString().slice(0, 10);
              const daySummary = summary ? summary[dateStr] : null;

              const taskCount = daySummary?.tasks?.length || 0;
              const habitCount = daySummary?.habits?.length || 0;
              const journalCount = daySummary?.journals?.length || 0;
              const noteCount = daySummary?.notes?.length || 0;
              const txCount = daySummary?.transactions?.length || 0;
              const evCount = daySummary?.events?.length || 0;

              const isSelected = selectedDateStr === dateStr;
              const isToday = new Date().toISOString().slice(0, 10) === dateStr;

              return (
                <Pressable
                  key={idx}
                  onPress={() => setSelectedDateStr(dateStr)}
                  style={[
                    styles.cell,
                    { borderColor: c.border },
                    !isCurrentMonth && { opacity: 0.25 },
                    isSelected && { backgroundColor: c.accentSoft, borderWidth: 1.5, borderColor: c.accent }
                  ]}
                >
                  <View
                    style={[
                      styles.dateBadge,
                      isToday && { backgroundColor: c.accent }
                    ]}
                  >
                    <AppText
                      variant="caption"
                      style={[
                        { fontWeight: "600" },
                        isToday && { color: "#FFFFFF" }
                      ]}
                    >
                      {date.getDate()}
                    </AppText>
                  </View>

                  {/* Indicator dots */}
                  <View style={styles.dotsRow}>
                    {taskCount > 0 && <View style={[styles.dot, { backgroundColor: "#10b981" }]} />}
                    {habitCount > 0 && <View style={[styles.dot, { backgroundColor: "#f43f5e" }]} />}
                    {journalCount > 0 && <View style={[styles.dot, { backgroundColor: "#3b82f6" }]} />}
                    {noteCount > 0 && <View style={[styles.dot, { backgroundColor: "#f59e0b" }]} />}
                    {txCount > 0 && <View style={[styles.dot, { backgroundColor: "#06b6d4" }]} />}
                    {evCount > 0 && <View style={[styles.dot, { backgroundColor: "#a855f7" }]} />}
                  </View>
                </Pressable>
              );
            })}
          </View>
        )}

        {/* Selected date details */}
        {selectedDateStr && activeDayDetails && (
          <View style={[styles.detailsContainer, { gap: space.lg }]}>
            <View style={[styles.detailsHeader, { borderBottomColor: c.border }]}>
              <View>
                <AppText variant="subtitle" style={{ fontWeight: "700" }}>
                  {new Date(selectedDateStr).toLocaleDateString("en-US", {
                    weekday: "long",
                    month: "short",
                    day: "numeric"
                  })}
                </AppText>
                <AppText variant="caption" color="muted">
                  Daily aggregated activity
                </AppText>
              </View>
              <Pressable
                onPress={() => setModalVisible(true)}
                style={[styles.miniFab, { backgroundColor: c.accent }]}
              >
                <Ionicons name="add" size={16} color="#FFFFFF" />
              </Pressable>
            </View>

            {/* Empty check */}
            {Object.values(activeDayDetails).every((arr: any) => arr.length === 0) && (
              <View style={styles.emptyView}>
                <Ionicons name="calendar-outline" size={32} color={c.textMuted} />
                <AppText variant="caption" color="muted" style={{ marginTop: space.xs }}>
                  No logs or events registered on this day.
                </AppText>
              </View>
            )}

            {/* 1. Events */}
            {activeDayDetails.events?.length > 0 && (
              <View style={{ gap: space.xs }}>
                <AppText variant="caption" color="muted" style={styles.sectionHeader}>
                  📆 Calendar Events
                </AppText>
                {activeDayDetails.events.map((ev: any) => (
                  <View
                    key={ev.id}
                    style={[styles.itemCard, { backgroundColor: c.card, borderColor: c.border }]}
                  >
                    <View style={{ flex: 1, gap: 2 }}>
                      <AppText variant="body" style={{ fontWeight: "600" }}>
                        {ev.title}
                      </AppText>
                      {ev.location && (
                        <AppText variant="caption" color="muted" style={{ flexDirection: "row", alignItems: "center" }}>
                          📍 {ev.location}
                        </AppText>
                      )}
                    </View>
                    <Pressable onPress={() => handleDeleteEvent(ev.id)} style={{ padding: 4 }}>
                      <Ionicons name="trash-outline" size={16} color={c.danger} />
                    </Pressable>
                  </View>
                ))}
              </View>
            )}

            {/* 2. Tasks */}
            {activeDayDetails.tasks?.length > 0 && (
              <View style={{ gap: space.xs }}>
                <AppText variant="caption" color="muted" style={styles.sectionHeader}>
                  ✔️ Tasks Completed / Due
                </AppText>
                {activeDayDetails.tasks.map((t: any) => (
                  <Pressable
                    key={t.id}
                    onPress={() => router.push(href("/(private)/(main)/tasks"))}
                    style={[styles.itemCard, { backgroundColor: c.card, borderColor: c.border }]}
                  >
                    <View style={{ flex: 1 }}>
                      <AppText variant="body" style={{ fontWeight: "600" }}>
                        {t.title}
                      </AppText>
                      <AppText variant="caption" color={t.status === "done" ? "success" : "warning"}>
                        {t.status}
                      </AppText>
                    </View>
                    <Ionicons name="chevron-forward" size={16} color={c.textMuted} />
                  </Pressable>
                ))}
              </View>
            )}

            {/* 3. Habits */}
            {activeDayDetails.habits?.length > 0 && (
              <View style={{ gap: space.xs }}>
                <AppText variant="caption" color="muted" style={styles.sectionHeader}>
                  🔥 Habits Logged
                </AppText>
                {activeDayDetails.habits.map((h: any) => (
                  <Pressable
                    key={h.id}
                    onPress={() => router.push(href("/(private)/(main)/habits"))}
                    style={[styles.itemCard, { backgroundColor: c.card, borderColor: c.border }]}
                  >
                    <View style={{ flex: 1 }}>
                      <AppText variant="body" style={{ fontWeight: "600" }}>
                        {h.name}
                      </AppText>
                      {h.note && (
                        <AppText variant="caption" color="muted" style={{ fontStyle: "italic" }}>
                          "{h.note}"
                        </AppText>
                      )}
                    </View>
                    <View style={[styles.colorBadge, { backgroundColor: h.color }]} />
                  </Pressable>
                ))}
              </View>
            )}

            {/* 4. Journals */}
            {activeDayDetails.journals?.length > 0 && (
              <View style={{ gap: space.xs }}>
                <AppText variant="caption" color="muted" style={styles.sectionHeader}>
                  📖 Journal Log
                </AppText>
                {activeDayDetails.journals.map((j: any) => (
                  <Pressable
                    key={j.id}
                    onPress={() => router.push(href("/(private)/(main)/journal"))}
                    style={[styles.itemCard, { backgroundColor: c.card, borderColor: c.border }]}
                  >
                    <View style={{ flex: 1 }}>
                      <AppText variant="body" style={{ fontWeight: "600" }}>
                        {j.title || "Untitled Entry"}
                      </AppText>
                      <AppText variant="caption" color="accent">
                        Mood: {j.mood}
                      </AppText>
                    </View>
                    <Ionicons name="chevron-forward" size={16} color={c.textMuted} />
                  </Pressable>
                ))}
              </View>
            )}

            {/* 5. Notes */}
            {activeDayDetails.notes?.length > 0 && (
              <View style={{ gap: space.xs }}>
                <AppText variant="caption" color="muted" style={styles.sectionHeader}>
                  📝 Notes Updated
                </AppText>
                {activeDayDetails.notes.map((n: any) => (
                  <Pressable
                    key={n.id}
                    onPress={() => router.push(href("/(private)/(main)/notes"))}
                    style={[styles.itemCard, { backgroundColor: c.card, borderColor: c.border }]}
                  >
                    <AppText variant="body" style={{ fontWeight: "600", flex: 1 }}>
                      {n.title || "Untitled Note"}
                    </AppText>
                    <Ionicons name="chevron-forward" size={16} color={c.textMuted} />
                  </Pressable>
                ))}
              </View>
            )}

            {/* 6. Transactions */}
            {activeDayDetails.transactions?.length > 0 && (
              <View style={{ gap: space.xs }}>
                <AppText variant="caption" color="muted" style={styles.sectionHeader}>
                  🪙 Transactions Recorded
                </AppText>
                {activeDayDetails.transactions.map((tx: any) => (
                  <Pressable
                    key={tx.id}
                    onPress={() => router.push(href("/(private)/(main)/finance"))}
                    style={[styles.itemCard, { backgroundColor: c.card, borderColor: c.border }]}
                  >
                    <View style={{ flex: 1 }}>
                      <AppText variant="body" style={{ fontWeight: "600" }}>
                        {tx.category || "General"}
                      </AppText>
                      {tx.note && <AppText variant="caption" color="muted">{tx.note}</AppText>}
                    </View>
                    <AppText
                      variant="body"
                      style={[
                        { fontWeight: "700" },
                        tx.kind === "expense" ? { color: c.danger } : { color: c.success }
                      ]}
                    >
                      {tx.kind === "expense" ? "-" : "+"}₹{Number(tx.amount).toLocaleString()}
                    </AppText>
                  </Pressable>
                ))}
              </View>
            )}
          </View>
        )}
      </ScrollView>

      {/* Add Event Modal */}
      <Modal animationType="slide" transparent visible={modalVisible} onRequestClose={() => setModalVisible(false)}>
        <View style={styles.modalOverlay}>
          <View style={[styles.modalContent, { backgroundColor: c.surfaceElevated }]}>
            <View style={styles.modalHeader}>
              <AppText variant="subtitle" style={{ fontWeight: "700" }}>
                New Event
              </AppText>
              <Pressable onPress={() => setModalVisible(false)}>
                <Ionicons name="close" size={24} color={c.textSecondary} />
              </Pressable>
            </View>

            <View style={{ gap: space.md, marginTop: space.md }}>
              <View style={{ gap: space.xs }}>
                <AppText variant="caption" color="muted" style={{ fontWeight: "600" }}>
                  TITLE
                </AppText>
                <TextInput
                  value={title}
                  onChangeText={setTitle}
                  placeholder="Meet with clients, dentist appointment, etc."
                  placeholderTextColor={c.textMuted}
                  style={[styles.input, { color: c.textPrimary, borderColor: c.border, backgroundColor: c.inputBg }]}
                />
              </View>

              <View style={styles.timeRow}>
                <View style={{ flex: 1, gap: space.xs }}>
                  <AppText variant="caption" color="muted" style={{ fontWeight: "600" }}>
                    STARTS (HH:MM)
                  </AppText>
                  <TextInput
                    value={starts}
                    onChangeText={setStarts}
                    placeholder="09:00"
                    placeholderTextColor={c.textMuted}
                    maxLength={5}
                    style={[styles.input, { color: c.textPrimary, borderColor: c.border, backgroundColor: c.inputBg }]}
                  />
                </View>

                <View style={{ flex: 1, gap: space.xs }}>
                  <AppText variant="caption" color="muted" style={{ fontWeight: "600" }}>
                    ENDS (HH:MM)
                  </AppText>
                  <TextInput
                    value={ends}
                    onChangeText={setEnds}
                    placeholder="10:00"
                    placeholderTextColor={c.textMuted}
                    maxLength={5}
                    style={[styles.input, { color: c.textPrimary, borderColor: c.border, backgroundColor: c.inputBg }]}
                  />
                </View>
              </View>

              <Pressable
                onPress={handleAddEvent}
                style={[styles.submitBtn, { backgroundColor: c.accent }]}
              >
                <AppText variant="body" style={{ color: "#FFFFFF", fontWeight: "700", textAlign: "center" }}>
                  Save Event
                </AppText>
              </Pressable>
            </View>
          </View>
        </View>
      </Modal>
    </Screen>
  );
}

const styles = StyleSheet.create({
  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    marginHorizontal: space.lg,
    marginTop: space.md,
    paddingVertical: space.sm,
    paddingHorizontal: space.md,
    borderRadius: radius.lg,
    borderWidth: 1
  },
  headerBtn: {
    padding: space.xs
  },
  weekdaysRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginHorizontal: space.lg,
    marginTop: space.md,
    marginBottom: space.xs
  },
  weekdayText: {
    width: "14.28%",
    textAlign: "center",
    fontWeight: "700"
  },
  grid: {
    flexDirection: "row",
    flexWrap: "wrap",
    marginHorizontal: space.lg,
    borderWidth: 0.5,
    borderRadius: radius.xl,
    overflow: "hidden"
  },
  cell: {
    width: "14.28%",
    aspectRatio: 0.95,
    justifyContent: "space-between",
    alignItems: "center",
    paddingVertical: 6,
    borderWidth: 0.5
  },
  dateBadge: {
    height: 24,
    width: 24,
    borderRadius: 12,
    alignItems: "center",
    justifyContent: "center"
  },
  dotsRow: {
    flexDirection: "row",
    justifyContent: "center",
    alignItems: "center",
    gap: 2,
    height: 6
  },
  dot: {
    height: 4,
    width: 4,
    borderRadius: 2
  },
  detailsContainer: {
    marginHorizontal: space.lg,
    marginTop: space.xl
  },
  detailsHeader: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    borderBottomWidth: 1,
    paddingBottom: space.sm
  },
  miniFab: {
    height: 28,
    width: 28,
    borderRadius: 14,
    alignItems: "center",
    justifyContent: "center"
  },
  emptyView: {
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: space.xl
  },
  sectionHeader: {
    fontWeight: "700",
    letterSpacing: 0.5,
    marginTop: space.xs
  },
  itemCard: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    padding: space.md,
    borderRadius: radius.md,
    borderWidth: 1
  },
  colorBadge: {
    height: 10,
    width: 10,
    borderRadius: 5
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: "rgba(0,0,0,0.5)",
    justifyContent: "flex-end"
  },
  modalContent: {
    borderTopLeftRadius: radius.xl,
    borderTopRightRadius: radius.xl,
    padding: space.lg,
    paddingBottom: 40
  },
  modalHeader: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    borderBottomWidth: 0.5,
    borderBottomColor: "rgba(0,0,0,0.1)",
    paddingBottom: space.md
  },
  input: {
    height: 44,
    borderWidth: 1,
    borderRadius: radius.md,
    paddingHorizontal: space.md,
    fontSize: 15
  },
  timeRow: {
    flexDirection: "row",
    gap: space.md
  },
  submitBtn: {
    height: 48,
    borderRadius: radius.lg,
    justifyContent: "center",
    alignItems: "center",
    marginTop: space.sm
  }
});
