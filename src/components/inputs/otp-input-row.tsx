import { useEffect, useRef, useState } from "react";
import { TextInput, View } from "react-native";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

type Props = {
  length?: number;
  value: string;
  onChange: (next: string) => void;
};

export function OtpInputRow({ length = 6, value, onChange }: Props) {
  const c = usePlannerTheme();
  const refs = useRef<Array<TextInput | null>>([]);
  const [cells, setCells] = useState<string[]>(() =>
    Array.from({ length }, (_, i) => value[i] ?? "")
  );

  useEffect(() => {
    setCells(Array.from({ length }, (_, i) => value[i] ?? ""));
  }, [length, value]);

  const commit = (nextCells: string[]) => {
    setCells(nextCells);
    onChange(nextCells.join(""));
  };

  return (
    <View style={{ flexDirection: "row", gap: space.sm, justifyContent: "space-between" }}>
      {cells.map((ch, idx) => (
        <TextInput
          key={idx}
          ref={(r) => {
            refs.current[idx] = r;
          }}
          keyboardType="number-pad"
          textContentType="oneTimeCode"
          maxLength={1}
          value={ch}
          onChangeText={(t) => {
            const digit = t.replace(/\D/g, "").slice(-1);
            const next = [...cells];
            next[idx] = digit;
            commit(next);
            if (digit && idx < length - 1) refs.current[idx + 1]?.focus();
          }}
          onKeyPress={({ nativeEvent }) => {
            if (nativeEvent.key === "Backspace" && !cells[idx] && idx > 0) {
              refs.current[idx - 1]?.focus();
            }
          }}
          style={{
            width: 48,
            height: 52,
            textAlign: "center",
            fontSize: 20,
            fontWeight: "600",
            color: c.textPrimary,
            backgroundColor: c.inputBg,
            borderRadius: radius.md,
            borderWidth: 1,
            borderColor: c.border,
          }}
        />
      ))}
    </View>
  );
}
