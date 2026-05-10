import type { ReactNode } from "react";
import { ActivityIndicator, Pressable, type ViewStyle } from "react-native";
import * as Haptics from "expo-haptics";
import Animated, { useAnimatedStyle, useSharedValue, withSpring } from "react-native-reanimated";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { AppText } from "@/components/typography/app-text";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";
import { motion } from "@/theme/animations/tokens";
import { useUiStore } from "@/stores/ui.store";

const APressable = Animated.createAnimatedComponent(Pressable);

type Props = {
  title: string;
  onPress: () => void | Promise<void>;
  loading?: boolean;
  disabled?: boolean;
  variant?: "primary" | "ghost";
  icon?: ReactNode;
  style?: ViewStyle;
};

export function PrimaryButton({
  title,
  onPress,
  loading,
  disabled,
  variant = "primary",
  icon,
  style,
}: Props) {
  const c = usePlannerTheme();
  const reduced = useUiStore((s) => s.reducedMotion);
  const scale = useSharedValue(1);
  const anim = useAnimatedStyle(() => ({
    transform: [{ scale: scale.value }],
  }));

  const busy = loading || disabled;

  return (
    <APressable
      accessibilityRole="button"
      disabled={busy}
      onPressIn={() => {
        if (!reduced) scale.value = withSpring(0.98, motion.spring);
      }}
      onPressOut={() => {
        if (!reduced) scale.value = withSpring(1, motion.spring);
      }}
      onPress={async () => {
        if (busy) return;
        try {
          await Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
        } catch {
          /* no haptics */
        }
        await onPress();
      }}
      style={[
        {
          height: 52,
          borderRadius: radius.md,
          paddingHorizontal: space.lg,
          alignItems: "center",
          justifyContent: "center",
          flexDirection: "row",
          gap: space.sm,
          backgroundColor: variant === "primary" ? c.accent : "transparent",
          borderWidth: variant === "ghost" ? 1 : 0,
          borderColor: c.border,
          opacity: disabled ? 0.45 : 1,
        },
        anim,
        style,
      ]}
    >
      {loading ? (
        <ActivityIndicator color={variant === "primary" ? "#0B0B10" : c.accent} />
      ) : (
        <>
          {icon}
          <AppText
            variant="subtitle"
            style={{
              color: variant === "primary" ? "#0B0B10" : c.textPrimary,
              fontWeight: "600",
            }}
          >
            {title}
          </AppText>
        </>
      )}
    </APressable>
  );
}
