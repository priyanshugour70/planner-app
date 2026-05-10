import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { tasksApi, type TasksListParams } from "@/modules/tasks/api/tasks-api";
import { tasksKeys } from "@/modules/tasks/hooks/query-keys";

function serializeListParams(p: TasksListParams): string {
  return JSON.stringify(p ?? {});
}

export function useTasksList(params: TasksListParams) {
  const key = serializeListParams(params);
  return useQuery({
    queryKey: tasksKeys.list(key),
    queryFn: () => tasksApi.list(params),
  });
}

function invalidateTasks(qc: ReturnType<typeof useQueryClient>) {
  return qc.invalidateQueries({ queryKey: tasksKeys.all });
}

export function useTaskMutations() {
  const qc = useQueryClient();
  const create = useMutation({
    mutationFn: (body: Record<string, unknown>) => tasksApi.create(body),
    onSuccess: () => invalidateTasks(qc),
  });
  const patch = useMutation({
    mutationFn: ({ id, body }: { id: string; body: Record<string, unknown> }) => tasksApi.patch(id, body),
    onSuccess: () => invalidateTasks(qc),
  });
  const remove = useMutation({
    mutationFn: (id: string) => tasksApi.delete(id),
    onSuccess: () => invalidateTasks(qc),
  });
  return { create, patch, remove };
}
