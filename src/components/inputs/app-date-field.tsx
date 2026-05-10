import { useEffect, useState } from "react";
import { Modal, Platform, Pressable, useColorScheme, View } from "react-native";
import DateTimePicker from "@react-native-community/datetimepicker";
import { AppText } from "@/components/typography/app-text";
import { formatDisplayDate } from "@/lib/format-display-date";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

function parseIsoToLocalDate(iso: string): Date {
  const [y, m, d] = iso.split("-").map((x) => Number(x));
  if (!Number.isFinite(y) || !Number.isFinite(m) || !Number.isFinite(d)) return new Date();
  return new Date(y, m - 1, d);
}

function localDateToIso(d: Date): string {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

type Props = {
  label: string;
  value: string;
  onChange: (iso: string) => void;
};

export function AppDateField({ label, value, onChange }: Props) {
  const c = usePlannerTheme();
  const systemScheme = useColorScheme();
  const [open, setOpen] = useState(false);
  const [draft, setDraft] = useState(() => parseIsoToLocalDate(value));

  useEffect(() => {
    if (open) setDraft(parseIsoToLocalDate(value));
  }, [open, value]);

  const confirm = () => {
    onChange(localDateToIso(draft));
    setOpen(false);
  };

  return (
    <View style={{ gap: space.xs }}>
      <AppText variant="caption" color="secondary">
        {label}
      </AppText>
      <Pressable
        onPress={() => setOpen(true)}
        accessibilityRole="button"
        accessibilityLabel={`${label}, ${formatDisplayDate(value)}`}
        style={{
          borderRadius: radius.md,
          borderWidth: 1,
          borderColor: c.border,
          paddingHorizontal: space.md,
          paddingVertical: space.sm + 2,
          backgroundColor: c.inputBg,
        }}
      >
        <AppText variant="body">{formatDisplayDate(value)}</AppText>
      </Pressable>

      {Platform.OS === "ios" ? (
        <Modal visible={open} animationType="slide" transparent onRequestClose={() => setOpen(false)}>
          <Pressable style={{ flex: 1, backgroundColor: c.overlay }} onPress={() => setOpen(false)}>
            <Pressable
              onPress={(e) => e.stopPropagation()}
              style={{
                marginTop: "auto",
                backgroundColor: c.surface,
                padding: space.lg,
                borderTopLeftRadius: radius.xl,
                borderTopRightRadius: radius.xl,
              }}
            >
              <AppText variant="subtitle" style={{ marginBottom: space.sm }}>
                Select date
              </AppText>
              <DateTimePicker
                value={draft}
                mode="date"
                display="inline"
                themeVariant={systemScheme === "dark" ? "dark" : "light"}
                onChange={(_, date) => {
                  if (date) setDraft(date);
                }}
              />
              <Pressable onPress={confirm} style={{ marginTop: space.md, alignSelf: "flex-end" }}>
                <AppText variant="subtitle" color="accent">
                  Done
                </AppText>
              </Pressable>
            </Pressable>
          </Pressable>
        </Modal>
      ) : null}

      {Platform.OS === "android" && open ? (
        <DateTimePicker
          value={parseIsoToLocalDate(value)}
          mode="date"
          display="default"
          onChange={(ev, date) => {
            setOpen(false);
            if (ev.type === "set" && date) onChange(localDateToIso(date));
          }}
        />
      ) : null}
    </View>
  );
}
