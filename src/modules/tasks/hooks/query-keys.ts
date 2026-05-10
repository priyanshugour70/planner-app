export const tasksKeys = {
  all: ["tasks"] as const,
  list: (p: string) => [...tasksKeys.all, "list", p] as const,
};
