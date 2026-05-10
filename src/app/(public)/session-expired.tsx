import { useRouter } from "expo-router";
import { href } from "@/navigation/href";
import { useEffect } from "react";
import { View } from "react-native";
import { PrimaryButton } from "@/components/buttons/primary-button";
import { Screen } from "@/components/ui/screen";
import { AppText } from "@/components/typography/app-text";
import { useAuthStore } from "@/stores/auth.store";
import { space } from "@/theme/spacing/tokens";

export default function SessionExpiredScreen() {
  const router = useRouter();

  useEffect(() => {
    return () => {
      useAuthStore.getState().clearSessionExpired();
    };
  }, []);

  return (
    <Screen>
      <View style={{ gap: space.md, marginTop: space.xxl }}>
        <AppText variant="title">Session ended</AppText>
        <AppText variant="body" color="secondary">
          For your security, please sign in again. Active sessions can be reviewed from the app settings.
        </AppText>
      </View>
      <View style={{ marginTop: space.xxl }}>
        <PrimaryButton
          title="Back to sign in"
          onPress={() => {
            useAuthStore.getState().clearSessionExpired();
            router.replace(href("/(public)/login"));
          }}
        />
      </View>
    </Screen>
  );
}
