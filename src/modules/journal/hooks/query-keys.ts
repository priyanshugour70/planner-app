export const journalKeys = {
  all: ["journal"] as const,
  list: (p?: string) => [...journalKeys.all, "list", p ?? ""] as const,
  entry: (id: string) => [...journalKeys.all, "entry", id] as const,
  analytics: () => [...journalKeys.all, "analytics"] as const,
};
