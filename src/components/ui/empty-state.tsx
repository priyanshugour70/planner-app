import { View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { AppText } from "@/components/typography/app-text";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

type Props = {
  icon: keyof typeof Ionicons.glyphMap;
  title: string;
  subtitle?: string;
};

export function EmptyState({ icon, title, subtitle }: Props) {
  const c = usePlannerTheme();
  return (
    <View
      style={{
        alignItems: "center",
        paddingVertical: space.xl,
        paddingHorizontal: space.lg,
        borderRadius: radius.lg,
        backgroundColor: c.surfaceElevated,
        borderWidth: 1,
        borderColor: c.border,
        borderStyle: "dashed",
      }}
    >
      <View
        style={{
          width: 52,
          height: 52,
          borderRadius: 26,
          alignItems: "center",
          justifyContent: "center",
          backgroundColor: c.accentSoft,
          marginBottom: space.md,
        }}
      >
        <Ionicons name={icon} size={26} color={c.accent} />
      </View>
      <AppText variant="subtitle" style={{ textAlign: "center" }}>
        {title}
      </AppText>
      {subtitle ? (
        <AppText variant="caption" color="secondary" style={{ marginTop: space.xs, textAlign: "center" }}>
          {subtitle}
        </AppText>
      ) : null}
    </View>
  );
}
