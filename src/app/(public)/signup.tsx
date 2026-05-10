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
import { useSignupMutation } from "@/modules/auth/hooks/use-auth-mutations";
import { signupSchema } from "@/modules/auth/validators";
import { space } from "@/theme/spacing/tokens";
import type { z } from "zod";

type Form = z.infer<typeof signupSchema>;

export default function SignupScreen() {
  const router = useRouter();
  const signup = useSignupMutation();
  const { control, handleSubmit, setError } = useForm<Form>({
    resolver: zodResolver(signupSchema),
    defaultValues: { username: "", email: "", password: "", fullName: "" },
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      await signup.mutateAsync({
        username: values.username,
        email: values.email,
        password: values.password,
        fullName: values.fullName?.trim() ? values.fullName.trim() : undefined,
      });
      router.replace(href("/(private)/(main)"));
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Could not create account";
      setError("password", { message: msg });
      Toast.show({ type: "error", text1: msg });
    }
  });

  return (
    <Screen scroll>
      <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : undefined} style={{ flex: 1 }}>
        <View style={{ gap: space.sm, marginBottom: space.lg }}>
          <AppText variant="title">Create account</AppText>
          <AppText variant="body" color="secondary">
            Password must match web requirements (12+ chars with mixed case, number, symbol).
          </AppText>
        </View>
        <View style={{ gap: space.md }}>
          <Controller
            control={control}
            name="username"
            render={({ field, fieldState }) => (
              <AppTextField
                label="Username"
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
            name="fullName"
            render={({ field, fieldState }) => (
              <AppTextField
                label="Full name (optional)"
                value={field.value}
                onBlur={field.onBlur}
                onChangeText={field.onChange}
                error={fieldState.error?.message}
              />
            )}
          />
          <Controller
            control={control}
            name="password"
            render={({ field, fieldState }) => (
              <AppTextField
                label="Password"
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
          <PrimaryButton title="Create account" loading={signup.isPending} onPress={onSubmit} />
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
