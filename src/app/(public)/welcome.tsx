import { useRouter } from "expo-router";
import { href } from "@/navigation/href";
import { View } from "react-native";
import { PrimaryButton } from "@/components/buttons/primary-button";
import { Screen } from "@/components/ui/screen";
import { AppText } from "@/components/typography/app-text";
import { space } from "@/theme/spacing/tokens";

export default function WelcomeScreen() {
  const router = useRouter();
  return (
    <Screen scroll>
      <View style={{ gap: space.md, marginTop: space.xxl }}>
        <AppText variant="display">Planner</AppText>
        <AppText variant="body" color="secondary">
          Calm, focused planning with finance, goals, and habits in one place.
        </AppText>
      </View>
      <View style={{ marginTop: space.xxxl, gap: space.md }}>
        <PrimaryButton title="Sign in" onPress={() => router.push(href("/(public)/login"))} />
        <PrimaryButton title="Create account" variant="ghost" onPress={() => router.push(href("/(public)/signup"))} />
        <PrimaryButton title="Email code sign-in" variant="ghost" onPress={() => router.push(href("/(public)/otp-login"))} />
      </View>
    </Screen>
  );
}
