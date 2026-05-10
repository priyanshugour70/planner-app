import { forwardRef } from "react";
import { TextInput, View, type TextInputProps } from "react-native";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { AppText } from "@/components/typography/app-text";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

type Props = TextInputProps & {
  label: string;
  error?: string;
};

export const AppTextField = forwardRef<TextInput, Props>(function AppTextField(
  { label, error, style, ...rest },
  ref
) {
  const c = usePlannerTheme();
  return (
    <View style={{ gap: space.xs }}>
      <AppText variant="caption" color="secondary">
        {label}
      </AppText>
      <TextInput
        ref={ref}
        placeholderTextColor={c.textMuted}
        style={[
          {
            borderRadius: radius.md,
            borderWidth: 1,
            borderColor: error ? c.danger : c.border,
            paddingHorizontal: space.md,
            paddingVertical: space.sm + 2,
            color: c.textPrimary,
            backgroundColor: c.inputBg,
            fontSize: 16,
          },
          style,
        ]}
        {...rest}
      />
      {error ? (
        <AppText variant="caption" color="danger">
          {error}
        </AppText>
      ) : null}
    </View>
  );
});
