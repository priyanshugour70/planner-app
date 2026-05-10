const enabled = __DEV__;

export const logger = {
  info: (...args: unknown[]) => {
    if (enabled) console.log("[planner]", ...args);
  },
  warn: (...args: unknown[]) => {
    if (enabled) console.warn("[planner]", ...args);
  },
  error: (...args: unknown[]) => {
    console.error("[planner]", ...args);
  },
};
