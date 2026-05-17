import { apiDeleteJson, apiGetJson, apiPatchJson, apiPostJson } from "@/services/api/http-client";
import type { JournalEntryDTO, JournalAnalyticsDTO } from "@/types/planner";

function q(params: Record<string, string | number | boolean | undefined | null>): string {
  const u = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) {
    if (v === undefined || v === null || v === "" || v === false) continue;
    u.set(k, typeof v === "boolean" ? "true" : String(v));
  }
  const s = u.toString();
  return s ? `?${s}` : "";
}

export type JournalListParams = {
  mood?: string;
  from?: string;
  to?: string;
  favoritesOnly?: boolean;
  q?: string;
  limit?: number;
};

export type JournalAnalyticsResponse = JournalAnalyticsDTO & {
  promptOfTheDay: string;
};

export const journalApi = {
  list: (opts?: JournalListParams) =>
    apiGetJson<JournalEntryDTO[]>(`/planner/journal${q(opts ?? {})}`),
  get: (id: string) =>
    apiGetJson<JournalEntryDTO>(`/planner/journal/${id}`),
  create: (body: Record<string, unknown>) =>
    apiPostJson<JournalEntryDTO>("/planner/journal", body),
  patch: (id: string, body: Record<string, unknown>) =>
    apiPatchJson<JournalEntryDTO>(`/planner/journal/${id}`, body),
  delete: (id: string) =>
    apiDeleteJson<{ ok: true }>(`/planner/journal/${id}`),
  analytics: () =>
    apiGetJson<JournalAnalyticsResponse>("/planner/journal/analytics"),
};
