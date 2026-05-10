import {
  apiDeleteJson,
  apiGetJson,
  apiGetWithMeta,
  apiPatchJson,
  apiPostJson,
} from "@/services/api/http-client";
import type {
  BudgetDTO,
  BudgetRollupDTO,
  DebtObligationDTO,
  DebtPaymentDTO,
  FinanceAccountDTO,
  FinanceCategoryDTO,
  FinanceSummaryDTO,
  RecurringMaterializeResultDTO,
  RecurringRuleDTO,
  TransactionDTO,
} from "@/types/planner";

function q(params: Record<string, string | number | undefined | null>): string {
  const u = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) {
    if (v === undefined || v === null || v === "") continue;
    u.set(k, String(v));
  }
  const s = u.toString();
  return s ? `?${s}` : "";
}

export const financeApi = {
  summary: () => apiGetJson<FinanceSummaryDTO>("/planner/finance/summary"),

  budgetRollup: () => apiGetJson<BudgetRollupDTO[]>("/planner/finance/budget-rollup"),

  budgets: {
    list: () => apiGetJson<BudgetDTO[]>("/planner/budgets"),
    get: (id: string) => apiGetJson<BudgetDTO>(`/planner/budgets/${id}`),
    create: (body: Record<string, unknown>) => apiPostJson<BudgetDTO>("/planner/budgets", body),
    patch: (id: string, body: Record<string, unknown>) =>
      apiPatchJson<BudgetDTO>(`/planner/budgets/${id}`, body),
    delete: (id: string) => apiDeleteJson<{ ok: true }>(`/planner/budgets/${id}`),
  },

  transactions: {
    list: (opts?: { from?: string; to?: string; limit?: number; cursor?: string }) =>
      apiGetWithMeta<TransactionDTO[]>(`/planner/transactions${q(opts ?? {})}`),
    get: (id: string) => apiGetJson<TransactionDTO>(`/planner/transactions/${id}`),
    create: (body: Record<string, unknown>) => apiPostJson<TransactionDTO>("/planner/transactions", body),
    patch: (id: string, body: Record<string, unknown>) =>
      apiPatchJson<TransactionDTO>(`/planner/transactions/${id}`, body),
    delete: (id: string) => apiDeleteJson<{ ok: true }>(`/planner/transactions/${id}`),
  },

  accounts: {
    list: () => apiGetJson<FinanceAccountDTO[]>("/planner/finance/accounts"),
    get: (id: string) => apiGetJson<FinanceAccountDTO>(`/planner/finance/accounts/${id}`),
    create: (body: Record<string, unknown>) =>
      apiPostJson<FinanceAccountDTO>("/planner/finance/accounts", body),
    patch: (id: string, body: Record<string, unknown>) =>
      apiPatchJson<FinanceAccountDTO>(`/planner/finance/accounts/${id}`, body),
    delete: (id: string) => apiDeleteJson<{ ok: true }>(`/planner/finance/accounts/${id}`),
  },

  categories: {
    list: () => apiGetJson<FinanceCategoryDTO[]>("/planner/finance/categories"),
    get: (id: string) => apiGetJson<FinanceCategoryDTO>(`/planner/finance/categories/${id}`),
    create: (body: Record<string, unknown>) =>
      apiPostJson<FinanceCategoryDTO>("/planner/finance/categories", body),
    patch: (id: string, body: Record<string, unknown>) =>
      apiPatchJson<FinanceCategoryDTO>(`/planner/finance/categories/${id}`, body),
    delete: (id: string) => apiDeleteJson<{ ok: true }>(`/planner/finance/categories/${id}`),
  },

  recurringRules: {
    list: () => apiGetJson<RecurringRuleDTO[]>("/planner/finance/recurring-rules"),
    create: (body: Record<string, unknown>) =>
      apiPostJson<RecurringRuleDTO>("/planner/finance/recurring-rules", body),
    patch: (id: string, body: Record<string, unknown>) =>
      apiPatchJson<RecurringRuleDTO>(`/planner/finance/recurring-rules/${id}`, body),
    delete: (id: string) => apiDeleteJson<{ ok: true }>(`/planner/finance/recurring-rules/${id}`),
    materializeDue: (body?: Record<string, unknown>) =>
      apiPostJson<RecurringMaterializeResultDTO>("/planner/finance/recurring-rules/materialize-due", body ?? {}),
  },

  debt: {
    obligations: {
      list: () => apiGetJson<DebtObligationDTO[]>("/planner/debt/obligations"),
      get: (id: string) => apiGetJson<DebtObligationDTO>(`/planner/debt/obligations/${id}`),
      create: (body: Record<string, unknown>) =>
        apiPostJson<DebtObligationDTO>("/planner/debt/obligations", body),
      patch: (id: string, body: Record<string, unknown>) =>
        apiPatchJson<DebtObligationDTO>(`/planner/debt/obligations/${id}`, body),
      delete: (id: string) => apiDeleteJson<{ ok: true }>(`/planner/debt/obligations/${id}`),
    },
    payments: {
      list: (obligationId: string) =>
        apiGetJson<DebtPaymentDTO[]>(`/planner/debt/obligations/${obligationId}/payments`),
      create: (obligationId: string, body: Record<string, unknown>) =>
        apiPostJson<DebtPaymentDTO>(`/planner/debt/obligations/${obligationId}/payments`, body),
    },
  },
};
