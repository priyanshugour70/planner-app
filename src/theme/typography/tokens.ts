import type { TextStyle } from "react-native";

export const typography = {
  display: {
    fontSize: 32,
    fontWeight: "700",
    letterSpacing: -0.6,
    lineHeight: 38,
  } satisfies TextStyle,
  title: {
    fontSize: 22,
    fontWeight: "600",
    letterSpacing: -0.3,
    lineHeight: 28,
  } satisfies TextStyle,
  subtitle: {
    fontSize: 17,
    fontWeight: "500",
    letterSpacing: -0.2,
    lineHeight: 22,
  } satisfies TextStyle,
  body: {
    fontSize: 16,
    fontWeight: "400",
    letterSpacing: -0.1,
    lineHeight: 22,
  } satisfies TextStyle,
  caption: {
    fontSize: 13,
    fontWeight: "400",
    letterSpacing: 0,
    lineHeight: 18,
  } satisfies TextStyle,
  mono: {
    fontSize: 14,
    fontWeight: "500",
    fontVariant: ["tabular-nums"],
    letterSpacing: 0.4,
  } satisfies TextStyle,
} as const;
