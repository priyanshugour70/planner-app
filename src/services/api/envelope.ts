import type { APIEnvelope } from "@/types/api-response";
import { isApiSuccess } from "@/types/api-response";

export function unwrapEnvelope<T>(env: APIEnvelope<T>): T {
  if (isApiSuccess(env)) return env.data;
  const code = env.error?.code ?? "UNKNOWN";
  const err = new Error(env.message || "Request failed");
  (err as Error & { code?: string; details?: unknown }).code = code;
  (err as Error & { details?: unknown }).details = env.error?.details;
  throw err;
}
