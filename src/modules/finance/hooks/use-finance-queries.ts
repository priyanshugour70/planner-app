import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { financeApi } from "@/modules/finance/api/finance-api";
import { financeKeys } from "@/modules/finance/hooks/query-keys";
import type { TransactionDTO } from "@/types/planner";

export function useFinanceSummary() {
  return useQuery({ queryKey: financeKeys.summary(), queryFn: () => financeApi.summary() });
}

export function useBudgetRollup() {
  return useQuery({ queryKey: financeKeys.rollup(), queryFn: () => financeApi.budgetRollup() });
}

export function useBudgets() {
  return useQuery({ queryKey: financeKeys.budgets(), queryFn: () => financeApi.budgets.list() });
}

export type TransactionsListResult = { rows: TransactionDTO[]; nextCursor: string | null };

export function useTransactionsList(opts?: { from?: string; to?: string; limit?: number }) {
  const q = JSON.stringify(opts ?? {});
  return useQuery({
    queryKey: financeKeys.transactions(q),
    queryFn: async (): Promise<TransactionsListResult> => {
      const { data, meta } = await financeApi.transactions.list(opts);
      const nextCursor = (meta?.nextCursor as string | null | undefined) ?? null;
      return { rows: data, nextCursor };
    },
  });
}

export function useAccounts() {
  return useQuery({ queryKey: financeKeys.accounts(), queryFn: () => financeApi.accounts.list() });
}

export function useCategories() {
  return useQuery({ queryKey: financeKeys.categories(), queryFn: () => financeApi.categories.list() });
}

export function useObligations() {
  return useQuery({ queryKey: financeKeys.obligations(), queryFn: () => financeApi.debt.obligations.list() });
}

export function useObligation(id: string) {
  return useQuery({
    queryKey: financeKeys.obligation(id),
    queryFn: () => financeApi.debt.obligations.get(id),
    enabled: Boolean(id),
  });
}

export function useDebtPayments(obligationId: string) {
  return useQuery({
    queryKey: financeKeys.payments(obligationId),
    queryFn: () => financeApi.debt.payments.list(obligationId),
    enabled: Boolean(obligationId),
  });
}

function invalidateAllFinance(qc: ReturnType<typeof useQueryClient>) {
  return qc.invalidateQueries({ queryKey: financeKeys.all });
}

export function useBudgetMutations() {
  const qc = useQueryClient();
  const create = useMutation({
    mutationFn: (body: Record<string, unknown>) => financeApi.budgets.create(body),
    onSuccess: () => invalidateAllFinance(qc),
  });
  const patch = useMutation({
    mutationFn: ({ id, body }: { id: string; body: Record<string, unknown> }) =>
      financeApi.budgets.patch(id, body),
    onSuccess: () => invalidateAllFinance(qc),
  });
  const remove = useMutation({
    mutationFn: (id: string) => financeApi.budgets.delete(id),
    onSuccess: () => invalidateAllFinance(qc),
  });
  return { create, patch, remove };
}

export function useTransactionMutations() {
  const qc = useQueryClient();
  const create = useMutation({
    mutationFn: (body: Record<string, unknown>) => financeApi.transactions.create(body),
    onSuccess: () => invalidateAllFinance(qc),
  });
  const patch = useMutation({
    mutationFn: ({ id, body }: { id: string; body: Record<string, unknown> }) =>
      financeApi.transactions.patch(id, body),
    onSuccess: () => invalidateAllFinance(qc),
  });
  const remove = useMutation({
    mutationFn: (id: string) => financeApi.transactions.delete(id),
    onSuccess: () => invalidateAllFinance(qc),
  });
  return { create, patch, remove };
}

export function useAccountMutations() {
  const qc = useQueryClient();
  const create = useMutation({
    mutationFn: (body: Record<string, unknown>) => financeApi.accounts.create(body),
    onSuccess: () => invalidateAllFinance(qc),
  });
  const patch = useMutation({
    mutationFn: ({ id, body }: { id: string; body: Record<string, unknown> }) =>
      financeApi.accounts.patch(id, body),
    onSuccess: () => invalidateAllFinance(qc),
  });
  const remove = useMutation({
    mutationFn: (id: string) => financeApi.accounts.delete(id),
    onSuccess: () => invalidateAllFinance(qc),
  });
  return { create, patch, remove };
}

export function useCategoryMutations() {
  const qc = useQueryClient();
  const create = useMutation({
    mutationFn: (body: Record<string, unknown>) => financeApi.categories.create(body),
    onSuccess: () => invalidateAllFinance(qc),
  });
  const patch = useMutation({
    mutationFn: ({ id, body }: { id: string; body: Record<string, unknown> }) =>
      financeApi.categories.patch(id, body),
    onSuccess: () => invalidateAllFinance(qc),
  });
  const remove = useMutation({
    mutationFn: (id: string) => financeApi.categories.delete(id),
    onSuccess: () => invalidateAllFinance(qc),
  });
  return { create, patch, remove };
}

export function useRecurringRules() {
  return useQuery({ queryKey: financeKeys.recurring(), queryFn: () => financeApi.recurringRules.list() });
}

export function useRecurringMutations() {
  const qc = useQueryClient();
  const create = useMutation({
    mutationFn: (body: Record<string, unknown>) => financeApi.recurringRules.create(body),
    onSuccess: () => invalidateAllFinance(qc),
  });
  const patch = useMutation({
    mutationFn: ({ id, body }: { id: string; body: Record<string, unknown> }) =>
      financeApi.recurringRules.patch(id, body),
    onSuccess: () => invalidateAllFinance(qc),
  });
  const remove = useMutation({
    mutationFn: (id: string) => financeApi.recurringRules.delete(id),
    onSuccess: () => invalidateAllFinance(qc),
  });
  const materializeDue = useMutation({
    mutationFn: (body?: Record<string, unknown>) => financeApi.recurringRules.materializeDue(body),
    onSuccess: () => invalidateAllFinance(qc),
  });
  return { create, patch, remove, materializeDue };
}

export function useDebtMutations() {
  const qc = useQueryClient();
  const createObligation = useMutation({
    mutationFn: (body: Record<string, unknown>) => financeApi.debt.obligations.create(body),
    onSuccess: () => invalidateAllFinance(qc),
  });
  const patchObligation = useMutation({
    mutationFn: ({ id, body }: { id: string; body: Record<string, unknown> }) =>
      financeApi.debt.obligations.patch(id, body),
    onSuccess: () => invalidateAllFinance(qc),
  });
  const deleteObligation = useMutation({
    mutationFn: (id: string) => financeApi.debt.obligations.delete(id),
    onSuccess: () => invalidateAllFinance(qc),
  });
  const createPayment = useMutation({
    mutationFn: ({ obligationId, body }: { obligationId: string; body: Record<string, unknown> }) =>
      financeApi.debt.payments.create(obligationId, body),
    onSuccess: async (_, vars) => {
      await qc.invalidateQueries({ queryKey: financeKeys.all });
      await qc.invalidateQueries({ queryKey: financeKeys.payments(vars.obligationId) });
    },
  });
  return { createObligation, patchObligation, deleteObligation, createPayment };
}
