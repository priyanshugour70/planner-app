import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { journalApi, type JournalListParams } from "@/modules/journal/api/journal-api";
import { journalKeys } from "@/modules/journal/hooks/query-keys";

function serializeParams(p?: JournalListParams): string {
  return JSON.stringify(p ?? {});
}

export function useJournalList(params?: JournalListParams) {
  return useQuery({
    queryKey: journalKeys.list(serializeParams(params)),
    queryFn: () => journalApi.list(params),
  });
}

export function useJournalEntry(id: string) {
  return useQuery({
    queryKey: journalKeys.entry(id),
    queryFn: () => journalApi.get(id),
    enabled: Boolean(id),
  });
}

export function useJournalAnalytics() {
  return useQuery({
    queryKey: journalKeys.analytics(),
    queryFn: () => journalApi.analytics(),
  });
}

function invalidateAll(qc: ReturnType<typeof useQueryClient>) {
  return qc.invalidateQueries({ queryKey: journalKeys.all });
}

export function useJournalMutations() {
  const qc = useQueryClient();
  const create = useMutation({
    mutationFn: (body: Record<string, unknown>) => journalApi.create(body),
    onSuccess: () => invalidateAll(qc),
  });
  const patch = useMutation({
    mutationFn: ({ id, body }: { id: string; body: Record<string, unknown> }) =>
      journalApi.patch(id, body),
    onSuccess: () => invalidateAll(qc),
  });
  const remove = useMutation({
    mutationFn: (id: string) => journalApi.delete(id),
    onSuccess: () => invalidateAll(qc),
  });
  const toggleFavorite = useMutation({
    mutationFn: ({ id, isFavorite }: { id: string; isFavorite: boolean }) =>
      journalApi.patch(id, { isFavorite }),
    onSuccess: () => invalidateAll(qc),
  });
  return { create, patch, remove, toggleFavorite };
}
