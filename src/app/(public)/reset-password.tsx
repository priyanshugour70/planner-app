import { zodResolver } from "@hookform/resolvers/zod";
import { useLocalSearchParams, useRouter } from "expo-router";
import { href } from "@/navigation/href";
import { useEffect } from "react";
import { Controller, useForm } from "react-hook-form";
import { KeyboardAvoidingView, Platform, Pressable, View } from "react-native";
import Toast from "react-native-toast-message";
import { PrimaryButton } from "@/components/buttons/primary-button";
import { AppTextField } from "@/components/inputs/app-text-field";
import { Screen } from "@/components/ui/screen";
import { AppText } from "@/components/typography/app-text";
import { useResetPasswordMutation } from "@/modules/auth/hooks/use-auth-mutations";
import { resetPasswordSchema } from "@/modules/auth/validators";
import { space } from "@/theme/spacing/tokens";
import type { z } from "zod";

type Form = z.infer<typeof resetPasswordSchema>;

export default function ResetPasswordScreen() {
  const router = useRouter();
  const params = useLocalSearchParams<{ email?: string }>();
  const reset = useResetPasswordMutation();
  const { control, handleSubmit, setValue } = useForm<Form>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: { email: params.email ?? "", code: "", newPassword: "" },
  });

  useEffect(() => {
    if (params.email) setValue("email", String(params.email));
  }, [params.email, setValue]);

  const onSubmit = handleSubmit(async (values) => {
    try {
      const res = await reset.mutateAsync(values);
      Toast.show({ type: "success", text1: res.message });
      router.replace(href("/(public)/login"));
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Could not reset password";
      Toast.show({ type: "error", text1: msg });
    }
  });

  return (
    <Screen scroll>
      <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : undefined} style={{ flex: 1 }}>
        <View style={{ gap: space.sm, marginBottom: space.lg }}>
          <AppText variant="title">Reset password</AppText>
          <AppText variant="body" color="secondary">
            Enter the code from your email and choose a new password.
          </AppText>
        </View>
        <View style={{ gap: space.md }}>
          <Controller
            control={control}
            name="email"
            render={({ field, fieldState }) => (
              <AppTextField
                label="Email"
                autoCapitalize="none"
                keyboardType="email-address"
                value={field.value}
                onBlur={field.onBlur}
                onChangeText={field.onChange}
                error={fieldState.error?.message}
              />
            )}
          />
          <Controller
            control={control}
            name="code"
            render={({ field, fieldState }) => (
              <AppTextField
                label="Reset code"
                autoCapitalize="none"
                value={field.value}
                onBlur={field.onBlur}
                onChangeText={field.onChange}
                error={fieldState.error?.message}
              />
            )}
          />
          <Controller
            control={control}
            name="newPassword"
            render={({ field, fieldState }) => (
              <AppTextField
                label="New password"
                secureTextEntry
                value={field.value}
                onBlur={field.onBlur}
                onChangeText={field.onChange}
                error={fieldState.error?.message}
              />
            )}
          />
        </View>
        <View style={{ marginTop: space.lg, gap: space.md }}>
          <PrimaryButton title="Update password" loading={reset.isPending} onPress={onSubmit} />
          <Pressable onPress={() => router.back()}>
            <AppText variant="caption" color="muted">
              Back
            </AppText>
          </Pressable>
        </View>
      </KeyboardAvoidingView>
    </Screen>
  );
}
