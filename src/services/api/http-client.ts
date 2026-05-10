import axios, {
  AxiosHeaders,
  isAxiosError,
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from "axios";
import { getApiV1Prefix } from "@/config/env";
import { PLANNER_CLIENT_HEADER, PLANNER_CLIENT_VALUE } from "@/constants/planner-client";
import type { APIEnvelope, APISuccessEnvelope } from "@/types/api-response";
import { isApiSuccess } from "@/types/api-response";
import { tokenMemory } from "@/services/auth/token-memory";
import { unwrapEnvelope } from "@/services/api/envelope";
import { randomUUID } from "@/lib/utils/uuid";
import { performRefreshRotation } from "@/services/auth/refresh-session";

declare module "axios" {
  export interface AxiosRequestConfig {
    /** When true, a 401 will not trigger refresh / replay. */
    skipAuthRefresh?: boolean;
  }
}

type RetriableConfig = InternalAxiosRequestConfig & { _retry?: boolean };

let refreshChain: Promise<boolean> | null = null;

function refreshSingleFlight(): Promise<boolean> {
  if (!refreshChain) {
    refreshChain = performRefreshRotation().finally(() => {
      refreshChain = null;
    });
  }
  return refreshChain;
}

export const http: AxiosInstance = axios.create({
  baseURL: getApiV1Prefix(),
  timeout: 30_000,
});

http.interceptors.request.use(async (config) => {
  const headers = AxiosHeaders.from(config.headers ?? {});
  if (!headers.has("X-Request-Id")) {
    headers.set("X-Request-Id", await randomUUID());
  }
  headers.set(PLANNER_CLIENT_HEADER, PLANNER_CLIENT_VALUE);
  const token = tokenMemory.get();
  if (token && !headers.has("Authorization")) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  config.headers = headers;
  return config;
});

http.interceptors.response.use(
  (res: AxiosResponse) => res,
  async (error) => {
    const original = error.config as RetriableConfig | undefined;
    if (!original || original.skipAuthRefresh) throw error;
    const status = error.response?.status;
    if (status !== 401 || original._retry) throw error;
    const authz =
      original.headers &&
      (AxiosHeaders.from(original.headers).get("Authorization") ??
        AxiosHeaders.from(original.headers).get("authorization"));
    if (!authz) throw error;
    original._retry = true;
    const ok = await refreshSingleFlight();
    if (!ok) {
      const { useAuthStore } = await import("@/stores/auth.store");
      await useAuthStore.getState().forceGuest();
      useAuthStore.getState().markSessionExpired();
      throw error;
    }
    const token = tokenMemory.get();
    if (token) {
      const headers = AxiosHeaders.from(original.headers ?? {});
      headers.set("Authorization", `Bearer ${token}`);
      original.headers = headers;
    }
    return http(original);
  }
);

function throwIfErrorEnvelope<T>(err: unknown): void {
  if (!isAxiosError(err)) return;
  const data = err.response?.data as APIEnvelope<T> | undefined;
  if (data && "success" in data && data.success === false) {
    const e = new Error(data.message);
    (e as Error & { code?: string }).code = data.error?.code;
    (e as Error & { details?: unknown }).details = data.error?.details;
    throw e;
  }
}

export async function apiPostJson<T>(
  path: string,
  body: unknown,
  cfg?: AxiosRequestConfig
): Promise<T> {
  try {
    const res = await http.post<APIEnvelope<T>>(path, body, {
      ...cfg,
      headers: {
        "Content-Type": "application/json",
        ...(cfg?.headers as object),
      },
    });
    return unwrapEnvelope(res.data);
  } catch (e) {
    throwIfErrorEnvelope<T>(e);
    throw e;
  }
}

export async function apiGetJson<T>(path: string, cfg?: AxiosRequestConfig): Promise<T> {
  try {
    const res = await http.get<APIEnvelope<T>>(path, cfg);
    return unwrapEnvelope(res.data);
  } catch (e) {
    throwIfErrorEnvelope<T>(e);
    throw e;
  }
}

export async function apiDeleteJson<T>(path: string, cfg?: AxiosRequestConfig): Promise<T> {
  try {
    const res = await http.delete<APIEnvelope<T>>(path, cfg);
    return unwrapEnvelope(res.data);
  } catch (e) {
    throwIfErrorEnvelope<T>(e);
    throw e;
  }
}

export async function apiPatchJson<T>(
  path: string,
  body: unknown,
  cfg?: AxiosRequestConfig
): Promise<T> {
  try {
    const res = await http.patch<APIEnvelope<T>>(path, body, {
      ...cfg,
      headers: {
        "Content-Type": "application/json",
        ...(cfg?.headers as object),
      },
    });
    return unwrapEnvelope(res.data);
  } catch (e) {
    throwIfErrorEnvelope<T>(e);
    throw e;
  }
}

/** GET returning `data` plus envelope `meta` (e.g. transaction cursor pagination). */
export async function apiGetWithMeta<T>(
  path: string,
  cfg?: AxiosRequestConfig
): Promise<{ data: T; meta: Record<string, unknown> }> {
  try {
    const res = await http.get<APIEnvelope<T>>(path, cfg);
    const env = res.data;
    if (!isApiSuccess(env)) {
      const e = new Error((env as { message?: string }).message ?? "Request failed");
      throw e;
    }
    const ok = env as APISuccessEnvelope<T>;
    return { data: ok.data, meta: ok.meta };
  } catch (e) {
    throwIfErrorEnvelope<T>(e);
    throw e;
  }
}
