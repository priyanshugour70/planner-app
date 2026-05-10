export const goalsKeys = {
  all: ["goals"] as const,
  list: () => [...goalsKeys.all, "list"] as const,
};
