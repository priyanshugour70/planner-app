export const habitsKeys = {
  all: ["habits"] as const,
  list: (archived?: boolean) => [...habitsKeys.all, "list", String(archived ?? false)] as const,
  entries: (habitId: string) => [...habitsKeys.all, "entries", habitId] as const,
  analytics: () => [...habitsKeys.all, "analytics"] as const,
};
