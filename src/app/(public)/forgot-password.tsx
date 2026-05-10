import { zodResolver } from "@hookform/resolvers/zod";
import { useRouter } from "expo-router";
import { href } from "@/navigation/href";
import { Controller, useForm } from "react-hook-form";
import { KeyboardAvoidingView, Platform, Pressable, View } from "react-native";
import Toast from "react-native-toast-message";
import { PrimaryButton } from "@/components/buttons/primary-button";
import { AppTextField } from "@/components/inputs/app-text-field";
import { Screen } from "@/components/ui/screen";
import { AppText } from "@/components/typography/app-text";
import { useForgotPasswordMutation } from "@/modules/auth/hooks/use-auth-mutations";
import { forgotSchema } from "@/modules/auth/validators";
import { space } from "@/theme/spacing/tokens";

type Form = { email: string };

export default function ForgotPasswordScreen() {
  const router = useRouter();
  const forgot = useForgotPasswordMutation();
  const { control, handleSubmit } = useForm<Form>({
    resolver: zodResolver(forgotSchema),
    defaultValues: { email: "" },
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      const res = await forgot.mutateAsync(values.email);
      Toast.show({ type: "success", text1: res.message });
      router.push({
        pathname: href("/(public)/reset-password"),
        params: { email: values.email },
      } as never);
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Request failed";
      Toast.show({ type: "error", text1: msg });
    }
  });

  return (
    <Screen scroll>
      <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : undefined} style={{ flex: 1 }}>
        <View style={{ gap: space.sm, marginBottom: space.lg }}>
          <AppText variant="title">Forgot password</AppText>
          <AppText variant="body" color="secondary">
            We will email a reset code if the account exists.
          </AppText>
        </View>
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
        <View style={{ marginTop: space.lg, gap: space.md }}>
          <PrimaryButton title="Send reset code" loading={forgot.isPending} onPress={onSubmit} />
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
