import { create } from "zustand";

/**
 * Lets the floating + button open the new-task modal on the tasks screen
 * (same idea as `useFinanceQuickAddStore`).
 */
type TasksQuickAddState = {
  /** Increment to signal "open create" without holding a stale object reference. */
  openCreateSignal: number;
  requestOpenCreate: () => void;
};

export const useTasksQuickAddStore = create<TasksQuickAddState>((set) => ({
  openCreateSignal: 0,
  requestOpenCreate: () => set((s) => ({ openCreateSignal: s.openCreateSignal + 1 })),
}));
