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
import { useSendOtpMutation } from "@/modules/auth/hooks/use-auth-mutations";
import { sendOtpSchema } from "@/modules/auth/validators";
import { space } from "@/theme/spacing/tokens";
import type { z } from "zod";

type Form = z.infer<typeof sendOtpSchema>;

export default function OtpLoginScreen() {
  const router = useRouter();
  const send = useSendOtpMutation();
  const { control, handleSubmit } = useForm<Form>({
    resolver: zodResolver(sendOtpSchema),
    defaultValues: { email: "", purpose: "login" },
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      const res = await send.mutateAsync({ email: values.email, purpose: "login" });
      if (res.devOtp) Toast.show({ type: "info", text1: `Dev OTP: ${res.devOtp}` });
      router.push({
        pathname: href("/(public)/otp-verify"),
        params: { email: values.email, purpose: "login" },
      } as never);
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Could not send code";
      Toast.show({ type: "error", text1: msg });
    }
  });

  return (
    <Screen scroll>
      <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : undefined} style={{ flex: 1 }}>
        <View style={{ gap: space.sm, marginBottom: space.lg }}>
          <AppText variant="title">Email code</AppText>
          <AppText variant="body" color="secondary">
            We will email a one-time code to sign you in.
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
          <PrimaryButton title="Send code" loading={send.isPending} onPress={onSubmit} />
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
