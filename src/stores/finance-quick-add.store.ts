import { create } from "zustand";

export type FinanceQuickAddTarget =
  | "transaction"
  | "budget"
  | "accounts"
  | "categories"
  | "debt"
  | "recurring";

type Pending = { id: number; target: FinanceQuickAddTarget };

type State = {
  pending: Pending | null;
  /** Queue a shortcut action; increments id so screens pick it up even when repeating the same target. */
  request: (target: FinanceQuickAddTarget) => void;
  clear: () => void;
};

let seq = 0;

export const useFinanceQuickAddStore = create<State>((set) => ({
  pending: null,
  request: (target) => set({ pending: { id: ++seq, target } }),
  clear: () => set({ pending: null }),
}));
