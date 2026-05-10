import { useQuery } from "@tanstack/react-query";
import { goalsApi } from "@/modules/goals/api/goals-api";
import { goalsKeys } from "@/modules/goals/hooks/query-keys";

export function useGoalsList() {
  return useQuery({
    queryKey: goalsKeys.list(),
    queryFn: () => goalsApi.list(),
  });
}
