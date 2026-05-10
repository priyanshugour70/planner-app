import type { ReactNode } from "react";
import { View } from "react-native";
import { AppText } from "@/components/typography/app-text";
import { space } from "@/theme/spacing/tokens";

type Props = {
  title: string;
  subtitle?: string;
  right?: ReactNode;
  /** Use screen-sized title for top-of-page headers. */
  largeTitle?: boolean;
};

export function SectionHeader({ title, subtitle, right, largeTitle }: Props) {
  return (
    <View style={{ flexDirection: "row", alignItems: "flex-start", justifyContent: "space-between", gap: space.md, marginBottom: space.sm }}>
      <View style={{ flex: 1, minWidth: 0 }}>
        <AppText variant={largeTitle ? "title" : "subtitle"}>{title}</AppText>
        {subtitle ? (
          <AppText variant="caption" color="secondary" style={{ marginTop: space.xs }}>
            {subtitle}
          </AppText>
        ) : null}
      </View>
      {right}
    </View>
  );
}
