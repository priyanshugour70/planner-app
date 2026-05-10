import { apiDeleteJson, apiGetJson, apiPatchJson, apiPostJson } from "@/services/api/http-client";
import type { TaskDTO } from "@/types/planner";

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

export type TasksListParams = {
  status?: string;
  goalId?: string;
  parentTaskId?: string;
  rootsOnly?: boolean;
  q?: string;
  limit?: number;
  sort?: "due" | "updated" | "priority" | "created";
};

export const tasksApi = {
  list: (opts?: TasksListParams) => apiGetJson<TaskDTO[]>(`/planner/tasks${q(opts ?? {})}`),
  create: (body: Record<string, unknown>) => apiPostJson<TaskDTO>("/planner/tasks", body),
  patch: (id: string, body: Record<string, unknown>) => apiPatchJson<TaskDTO>(`/planner/tasks/${id}`, body),
  delete: (id: string) => apiDeleteJson<{ ok: true }>(`/planner/tasks/${id}`),
};
