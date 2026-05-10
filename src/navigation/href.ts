import type { Href } from "expo-router";

/** Bridges literal route strings to Expo Router's `Href` typing. */
export function href(path: string): Href {
  return path as Href;
}
