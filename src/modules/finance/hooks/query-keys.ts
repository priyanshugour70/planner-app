export const financeKeys = {
  all: ["finance"] as const,
  summary: () => [...financeKeys.all, "summary"] as const,
  rollup: () => [...financeKeys.all, "rollup"] as const,
  budgets: () => [...financeKeys.all, "budgets"] as const,
  budget: (id: string) => [...financeKeys.budgets(), id] as const,
  transactions: (q?: string) => [...financeKeys.all, "transactions", q ?? ""] as const,
  accounts: () => [...financeKeys.all, "accounts"] as const,
  categories: () => [...financeKeys.all, "categories"] as const,
  obligations: () => [...financeKeys.all, "obligations"] as const,
  obligation: (id: string) => [...financeKeys.obligations(), id] as const,
  payments: (obligationId: string) => [...financeKeys.obligations(), obligationId, "payments"] as const,
  recurring: () => [...financeKeys.all, "recurring"] as const,
};
