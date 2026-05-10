import { useLocalSearchParams, useRouter } from "expo-router";
import { href } from "@/navigation/href";
import { useMemo, useState } from "react";
import { Pressable, View } from "react-native";
import Toast from "react-native-toast-message";
import { PrimaryButton } from "@/components/buttons/primary-button";
import { OtpInputRow } from "@/components/inputs/otp-input-row";
import { Screen } from "@/components/ui/screen";
import { AppText } from "@/components/typography/app-text";
import { useSendOtpMutation, useVerifyOtpMutation } from "@/modules/auth/hooks/use-auth-mutations";
import type { OtpPurpose } from "@/modules/auth/validators";
import { otpPurposeSchema } from "@/modules/auth/validators";
import { space } from "@/theme/spacing/tokens";

export default function OtpVerifyScreen() {
  const router = useRouter();
  const params = useLocalSearchParams<{ email?: string | string[]; purpose?: string | string[] }>();
  const email = String(Array.isArray(params.email) ? params.email[0] : params.email ?? "");
  const purpose = useMemo(() => {
    const raw = Array.isArray(params.purpose) ? params.purpose[0] : params.purpose;
    const p = otpPurposeSchema.safeParse(raw);
    return (p.success ? p.data : "login") as OtpPurpose;
  }, [params.purpose]);

  const [code, setCode] = useState("");
  const verify = useVerifyOtpMutation();
  const resend = useSendOtpMutation();

  const onVerify = async () => {
    try {
      await verify.mutateAsync({ email, code, purpose });
      if (purpose === "login") router.replace(href("/(private)/(main)"));
      else {
        Toast.show({ type: "success", text1: "Verified" });
        router.replace(href("/(public)/welcome"));
      }
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Invalid code";
      Toast.show({ type: "error", text1: msg });
    }
  };

  const onResend = async () => {
    try {
      const res = await resend.mutateAsync({ email, purpose });
      if (res.devOtp) Toast.show({ type: "info", text1: `Dev OTP: ${res.devOtp}` });
      Toast.show({ type: "success", text1: "Code sent" });
    } catch (e) {
      const msg = e instanceof Error ? e.message : "Could not resend";
      Toast.show({ type: "error", text1: msg });
    }
  };

  return (
    <Screen scroll>
      <View style={{ gap: space.sm, marginBottom: space.lg }}>
        <AppText variant="title">Enter code</AppText>
        <AppText variant="body" color="secondary">
          Sent to {email}
        </AppText>
      </View>
      <OtpInputRow value={code} onChange={setCode} />
      <View style={{ marginTop: space.lg, gap: space.md }}>
        <PrimaryButton title="Verify" loading={verify.isPending} disabled={code.length < 4} onPress={onVerify} />
        <PrimaryButton title="Resend code" variant="ghost" loading={resend.isPending} onPress={onResend} />
        <Pressable onPress={() => router.back()}>
          <AppText variant="caption" color="muted">
            Back
          </AppText>
        </Pressable>
      </View>
    </Screen>
  );
}
