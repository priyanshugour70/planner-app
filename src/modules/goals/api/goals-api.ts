import { apiGetJson } from "@/services/api/http-client";
import type { GoalDTO } from "@/types/planner";

export const goalsApi = {
  list: () => apiGetJson<GoalDTO[]>("/planner/goals"),
};
