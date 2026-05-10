import axios from "axios";
import { getApiV1Prefix } from "@/config/env";
import { PLANNER_CLIENT_HEADER, PLANNER_CLIENT_VALUE } from "@/constants/planner-client";
import type { APIEnvelope } from "@/types/api-response";
import { isApiSuccess } from "@/types/api-response";
import type { AuthSessionPayload } from "@/types/auth";
import { tokenMemory } from "@/services/auth/token-memory";
import { readRefreshToken, saveRefreshToken } from "@/services/storage/secure-tokens";
import { randomUUID } from "@/lib/utils/uuid";

export async function performRefreshRotation(): Promise<boolean> {
  const refreshToken = await readRefreshToken();
  if (!refreshToken) return false;
  try {
    const res = await axios.post<APIEnvelope<AuthSessionPayload>>(
      `${getApiV1Prefix()}/auth/refresh`,
      { refreshToken },
      {
        headers: {
          "Content-Type": "application/json",
          [PLANNER_CLIENT_HEADER]: PLANNER_CLIENT_VALUE,
          "X-Request-Id": await randomUUID(),
        },
        validateStatus: () => true,
      }
    );
    const env = res.data;
    if (!isApiSuccess(env) || !env.data.accessToken) return false;
    const data = env.data;
    tokenMemory.set(data.accessToken);
    if (data.refreshToken) await saveRefreshToken(data.refreshToken);
    const { useAuthStore } = await import("@/stores/auth.store");
    useAuthStore.getState().applyRefreshedSession(data);
    return true;
  } catch {
    return false;
  }
}
