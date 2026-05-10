import { create } from "zustand";

type State = {
  reducedMotion: boolean;
  setReducedMotion: (v: boolean) => void;
};

export const useUiStore = create<State>((set) => ({
  reducedMotion: false,
  setReducedMotion: (reducedMotion) => set({ reducedMotion }),
}));
