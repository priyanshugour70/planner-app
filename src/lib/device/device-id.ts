import * as Application from "expo-application";
import * as SecureStore from "expo-secure-store";
import { Platform } from "react-native";
import { randomUUID } from "@/lib/utils/uuid";

const KEY = "planner.device_id";

export async function getOrCreateDeviceId(): Promise<string> {
  const existing = await SecureStore.getItemAsync(KEY);
  if (existing) return existing;

  let next = "";
  try {
    if (Platform.OS === "ios") {
      next = (await Application.getIosIdForVendorAsync()) ?? "";
    } else if (Platform.OS === "android") {
      next = (await Application.getAndroidId()) ?? "";
    }
  } catch {
    next = "";
  }
  if (!next) next = await randomUUID();
  await SecureStore.setItemAsync(KEY, next, {
    keychainAccessible: SecureStore.WHEN_UNLOCKED_THIS_DEVICE_ONLY,
  });
  return next;
}
