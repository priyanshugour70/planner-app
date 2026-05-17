import { apiDeleteJson, apiGetJson, apiPatchJson, apiPostJson } from "@/services/api/http-client";
import type { HabitDTO, HabitEntryDTO, HabitAnalyticsDTO, HabitsSummaryDTO } from "@/types/planner";

function q(params: Record<string, string | number | boolean | undefined | null>): string {
  const u = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) {
    if (v === undefined || v === null || v === "") continue;
    if (v === false) continue;
    u.set(k, typeof v === "boolean" ? "true" : String(v));
  }
  const s = u.toString();
  return s ? `?${s}` : "";
}

export type HabitsListParams = { archived?: boolean };

export type HabitsAnalyticsResponse = {
  habits: HabitAnalyticsDTO[];
  summary: HabitsSummaryDTO;
};

export const habitsApi = {
  list: (opts?: HabitsListParams) =>
    apiGetJson<HabitDTO[]>(`/planner/habits${q({ archived: opts?.archived })}`),
  create: (body: Record<string, unknown>) =>
    apiPostJson<HabitDTO>("/planner/habits", body),
  patch: (id: string, body: Record<string, unknown>) =>
    apiPatchJson<HabitDTO>(`/planner/habits/${id}`, body),
  delete: (id: string) =>
    apiDeleteJson<{ ok: true }>(`/planner/habits/${id}`),

  entries: {
    list: (habitId: string, opts?: { from?: string; to?: string; limit?: number }) =>
      apiGetJson<HabitEntryDTO[]>(`/planner/habits/${habitId}/entries${q(opts ?? {})}`),
    log: (habitId: string, body: Record<string, unknown>) =>
      apiPostJson<HabitEntryDTO>(`/planner/habits/${habitId}/entries`, body),
    delete: (habitId: string, entryId: string) =>
      apiDeleteJson<{ ok: true }>(`/planner/habits/${habitId}/entries/${entryId}`),
  },

  analytics: () => apiGetJson<HabitsAnalyticsResponse>("/planner/habits/analytics"),
};
