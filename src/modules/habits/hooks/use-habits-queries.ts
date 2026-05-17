import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { habitsApi, type HabitsListParams } from "@/modules/habits/api/habits-api";
import { habitsKeys } from "@/modules/habits/hooks/query-keys";

export function useHabitsList(params?: HabitsListParams) {
  return useQuery({
    queryKey: habitsKeys.list(params?.archived),
    queryFn: () => habitsApi.list(params),
  });
}

export function useHabitEntries(habitId: string) {
  return useQuery({
    queryKey: habitsKeys.entries(habitId),
    queryFn: () => habitsApi.entries.list(habitId, { limit: 60 }),
    enabled: Boolean(habitId),
  });
}

export function useHabitsAnalytics() {
  return useQuery({
    queryKey: habitsKeys.analytics(),
    queryFn: () => habitsApi.analytics(),
  });
}

function invalidateAll(qc: ReturnType<typeof useQueryClient>) {
  return qc.invalidateQueries({ queryKey: habitsKeys.all });
}

export function useHabitMutations() {
  const qc = useQueryClient();
  const create = useMutation({
    mutationFn: (body: Record<string, unknown>) => habitsApi.create(body),
    onSuccess: () => invalidateAll(qc),
  });
  const patch = useMutation({
    mutationFn: ({ id, body }: { id: string; body: Record<string, unknown> }) =>
      habitsApi.patch(id, body),
    onSuccess: () => invalidateAll(qc),
  });
  const remove = useMutation({
    mutationFn: (id: string) => habitsApi.delete(id),
    onSuccess: () => invalidateAll(qc),
  });
  const logEntry = useMutation({
    mutationFn: ({ habitId, body }: { habitId: string; body: Record<string, unknown> }) =>
      habitsApi.entries.log(habitId, body),
    onSuccess: () => invalidateAll(qc),
  });
  const removeEntry = useMutation({
    mutationFn: ({ habitId, entryId }: { habitId: string; entryId: string }) =>
      habitsApi.entries.delete(habitId, entryId),
    onSuccess: () => invalidateAll(qc),
  });
  return { create, patch, remove, logEntry, removeEntry };
}
