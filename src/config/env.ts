import Constants from "expo-constants";

function readEnv(key: string): string | undefined {
  const fromProcess = process.env[key as keyof NodeJS.ProcessEnv];
  if (typeof fromProcess === "string" && fromProcess.trim()) return fromProcess.trim();
  const extra = Constants.expoConfig?.extra as Record<string, string> | undefined;
  const fromExtra = extra?.[key];
  if (typeof fromExtra === "string" && fromExtra.trim()) return fromExtra.trim();
  return undefined;
}

/**
 * Base URL of the Next.js app (no trailing slash), e.g. `http://127.0.0.1:3000`.
 * Set `EXPO_PUBLIC_PLANNER_API_URL` in `.env` or `app.config.js`.
 */
export function getApiBaseUrl(): string {
  const raw = readEnv("EXPO_PUBLIC_PLANNER_API_URL") ?? "https://planner.lssgoo.com";
  return raw.replace(/\/+$/, "");
}

export function getApiV1Prefix(): string {
  return `${getApiBaseUrl()}/api/v1`;
}
