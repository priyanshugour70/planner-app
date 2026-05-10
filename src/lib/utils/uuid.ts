import * as Crypto from "expo-crypto";

export async function randomUUID(): Promise<string> {
  return Crypto.randomUUID();
}
