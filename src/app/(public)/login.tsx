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
import { useLoginMutation } from "@/modules/auth/hooks/use-auth-mutations";
import { loginSchema } from "@/modules/auth/validators";
import { space } from "@/theme/spacing/tokens";

type Form = { email: string; password: string };

export default function LoginScreen() {
  const router = useRouter();
  const login = useLoginMutation();
  const { control, handleSubmit, setError } = useForm<Form>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      await login.mutateAsync(values);
      router.replace(href("/(private)/(main)"));
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Could not sign in";
      const code = (e as Error & { code?: string }).code;
      if (code === "ACCOUNT_LOCKED") setError("password", { message: msg });
      else setError("password", { message: msg });
      Toast.show({ type: "error", text1: msg });
    }
  });

  return (
    <Screen>
      <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : undefined} style={{ flex: 1 }}>
        <View style={{ gap: space.sm, marginBottom: space.lg }}>
          <AppText variant="title">Sign in</AppText>
          <AppText variant="body" color="secondary">
            Use the same account as the web app.
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
            name="password"
            render={({ field, fieldState }) => (
              <AppTextField
                label="Password"
                secureTextEntry
                value={field.value}
                onChangeText={field.onChange}
                error={fieldState.error?.message}
              />
            )}
          />
        </View>
        <View style={{ marginTop: space.lg, gap: space.md }}>
          <PrimaryButton title="Continue" loading={login.isPending} onPress={onSubmit} />
          <Pressable onPress={() => router.push(href("/(public)/forgot-password"))}>
            <AppText variant="caption" color="accent">
              Forgot password?
            </AppText>
          </Pressable>
          <Pressable onPress={() => router.push(href("/(public)/welcome"))}>
            <AppText variant="caption" color="muted">
              Back
            </AppText>
          </Pressable>
        </View>
      </KeyboardAvoidingView>
    </Screen>
  );
}
