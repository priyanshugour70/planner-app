import { Ionicons } from "@expo/vector-icons";
import type { ReactNode, RefObject } from "react";
import { createContext, useContext } from "react";
import { Pressable } from "react-native";
import type { MainNavMenuSheetRef } from "@/components/navigation/main-nav-menu-sheet";
import { usePlannerTheme } from "@/providers/planner-theme-provider";

const Ctx = createContext<RefObject<MainNavMenuSheetRef | null> | null>(null);

export function MainMenuRefProvider({
  children,
  menuRef,
}: {
  children: ReactNode;
  menuRef: RefObject<MainNavMenuSheetRef | null>;
}) {
  return <Ctx.Provider value={menuRef}>{children}</Ctx.Provider>;
}

export function useMainMenuRef(): RefObject<MainNavMenuSheetRef | null> {
  const v = useContext(Ctx);
  if (!v) throw new Error("useMainMenuRef must be used within MainMenuRefProvider");
  return v;
}

export function MainMenuHeaderButton() {
  const ref = useMainMenuRef();
  const c = usePlannerTheme();
  return (
    <Pressable
      onPress={() => ref.current?.present()}
      hitSlop={12}
      accessibilityLabel="Open navigation menu"
      style={{
        marginRight: 8,
        paddingVertical: 8,
        paddingHorizontal: 10,
        borderRadius: 12,
        backgroundColor: c.surfaceElevated,
        borderWidth: 1,
        borderColor: c.border,
      }}
    >
      <Ionicons name="menu-outline" size={22} color={c.accent} />
    </Pressable>
  );
}
