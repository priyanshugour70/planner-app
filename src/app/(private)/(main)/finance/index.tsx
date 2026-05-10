import { useRouter } from "expo-router";
import { Pressable, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { href } from "@/navigation/href";
import { AppText } from "@/components/typography/app-text";
import { ProgressBar } from "@/components/ui/progress-bar";
import { Screen } from "@/components/ui/screen";
import { SectionHeader } from "@/components/ui/section-header";
import { SkeletonBlock } from "@/components/ui/skeleton";
import { StatTile } from "@/components/ui/stat-tile";
import { formatInrAmount } from "@/lib/format-inr";
import { useBudgetRollup, useFinanceSummary } from "@/modules/finance/hooks/use-finance-queries";
import type { BudgetRollupDTO } from "@/types/planner";
import { usePlannerTheme } from "@/providers/planner-theme-provider";
import { radius } from "@/theme/radius/tokens";
import { space } from "@/theme/spacing/tokens";

function NavCard({
  title,
  subtitle,
  icon,
  iconTint,
  onPress,
}: {
  title: string;
  subtitle?: string;
  icon: keyof typeof Ionicons.glyphMap;
  iconTint: string;
  onPress: () => void;
}) {
  const c = usePlannerTheme();
  return (
    <Pressable
      onPress={onPress}
      style={{
        flexDirection: "row",
        alignItems: "center",
        gap: space.md,
        padding: space.lg,
        borderRadius: radius.lg,
        backgroundColor: c.card,
        borderWidth: 1,
        borderColor: c.border,
        marginBottom: space.sm,
      }}
    >
      <View
        style={{
          width: 48,
          height: 48,
          borderRadius: radius.md,
          alignItems: "center",
          justifyContent: "center",
          backgroundColor: c.accentSoft,
          borderWidth: 1,
          borderColor: iconTint + "55",
        }}
      >
        <Ionicons name={icon} size={24} color={iconTint} />
      </View>
      <View style={{ flex: 1, minWidth: 0 }}>
        <AppText variant="subtitle">{title}</AppText>
        {subtitle ? (
          <AppText variant="caption" color="secondary" style={{ marginTop: space.xs }}>
            {subtitle}
          </AppText>
        ) : null}
      </View>
      <Ionicons name="chevron-forward" size={20} color={c.textMuted} />
    </Pressable>
  );
}

function RollupRow({ row }: { row: BudgetRollupDTO }) {
  const c = usePlannerTheme();
  const spent = Number(row.spent);
  const limit = Number(row.amountLimit);
  const pct = limit > 0 && Number.isFinite(spent) && Number.isFinite(limit) ? Math.min(100, (spent / limit) * 100) : 0;
  const over = limit > 0 && spent > limit;

  return (
    <View style={{ paddingVertical: space.md, borderBottomWidth: 1, borderBottomColor: c.border }}>
      <View style={{ flexDirection: "row", justifyContent: "space-between", alignItems: "flex-start", gap: space.md }}>
        <AppText variant="body" style={{ flex: 1 }}>
          {row.name}
        </AppText>
        <AppText variant="caption" color="secondary" style={{ textAlign: "right" }}>
          {formatInrAmount(row.spent)} / {formatInrAmount(row.amountLimit)}
        </AppText>
      </View>
      <ProgressBar value={pct} overBudget={over} />
      {over ? (
        <AppText variant="caption" color="warning" style={{ marginTop: space.xs }}>
          Over limit — adjust spend or raise the cap.
        </AppText>
      ) : null}
    </View>
  );
}

export default function FinanceDashboardScreen() {
  const router = useRouter();
  const c = usePlannerTheme();
  const summary = useFinanceSummary();
  const rollup = useBudgetRollup();

  if (summary.isPending || rollup.isPending) {
    return (
      <Screen scroll showBackLink={false}>
        <SkeletonBlock height={22} width="40%" />
        <SkeletonBlock height={14} width="70%" style={{ marginTop: space.md }} />
        <View style={{ flexDirection: "row", flexWrap: "wrap", gap: space.sm, marginTop: space.xl }}>
          <View style={{ flex: 1, minWidth: "45%" }}>
            <SkeletonBlock height={88} />
          </View>
          <View style={{ flex: 1, minWidth: "45%" }}>
            <SkeletonBlock height={88} />
          </View>
          <View style={{ flex: 1, minWidth: "45%" }}>
            <SkeletonBlock height={88} />
          </View>
          <View style={{ flex: 1, minWidth: "45%" }}>
            <SkeletonBlock height={88} />
          </View>
        </View>
        <SkeletonBlock height={120} style={{ marginTop: space.lg }} />
      </Screen>
    );
  }

  const s = summary.data;

  return (
    <Screen scroll showBackLink={false}>
      <View
        style={{
          marginBottom: space.lg,
          padding: space.lg,
          borderRadius: radius.lg,
          backgroundColor: c.accentSoft,
          borderWidth: 1,
          borderColor: c.accent + "44",
        }}
      >
        <AppText variant="caption" color="accent" style={{ fontWeight: "600", letterSpacing: 0.5 }}>
          FINANCE
        </AppText>
        <AppText variant="title" style={{ marginTop: space.xs }}>
          Your money at a glance
        </AppText>
        <AppText variant="body" color="secondary" style={{ marginTop: space.sm }}>
          Track income, budgets, accounts, and debt in one place.
        </AppText>
      </View>

      <SectionHeader largeTitle title="Overview" subtitle="This month and open items" />

      {s ? (
        <View style={{ gap: space.sm, marginBottom: space.lg }}>
          <View style={{ flexDirection: "row", flexWrap: "wrap", gap: space.sm }}>
            <StatTile label="Month income" value={formatInrAmount(s.monthIncome)} tone="income" />
            <StatTile label="Month spend" value={formatInrAmount(s.monthSpend)} tone="spend" />
          </View>
          <View style={{ flexDirection: "row", flexWrap: "wrap", gap: space.sm }}>
            <StatTile
              label="Open debt"
              value={
                <AppText variant="subtitle">
                  {s.openDebtCount} · {formatInrAmount(s.openDebtExposure)}
                </AppText>
              }
              tone="warning"
              footer="Active obligations you owe or are owed"
            />
            <StatTile label="Budgets" value={String(s.budgetCount)} tone="accent" footer="Plans with limits" />
          </View>
        </View>
      ) : null}

      {rollup.data?.length ? (
        <View style={{ marginBottom: space.lg }}>
          <SectionHeader title="Budget vs spend" subtitle="How much you have used against each cap" />
          <View
            style={{
              borderRadius: radius.lg,
              borderWidth: 1,
              borderColor: c.border,
              backgroundColor: c.surfaceElevated,
              paddingHorizontal: space.md,
            }}
          >
            {rollup.data.slice(0, 8).map((r) => (
              <RollupRow key={r.budgetId} row={r} />
            ))}
          </View>
        </View>
      ) : null}

      <SectionHeader title="Manage" subtitle="Jump into a detailed workspace" />
      <NavCard
        title="Transactions"
        subtitle="Income, expenses, and ledger entries"
        icon="swap-horizontal-outline"
        iconTint={c.accent}
        onPress={() => router.push(href("/(private)/(main)/finance/transactions"))}
      />
      <NavCard
        title="Budgets"
        subtitle="Periods, limits, and categories"
        icon="pie-chart-outline"
        iconTint={c.warning}
        onPress={() => router.push(href("/(private)/(main)/finance/budgets"))}
      />
      <NavCard
        title="Accounts"
        subtitle="Cash, bank, and currency"
        icon="wallet-outline"
        iconTint={c.success}
        onPress={() => router.push(href("/(private)/(main)/finance/accounts"))}
      />
      <NavCard
        title="Categories"
        subtitle="Group spending and income"
        icon="pricetags-outline"
        iconTint={c.accent}
        onPress={() => router.push(href("/(private)/(main)/finance/categories"))}
      />
      <NavCard
        title="Debt & receivables"
        subtitle="Who owes whom, payments, status"
        icon="document-text-outline"
        iconTint={c.danger}
        onPress={() => router.push(href("/(private)/(main)/finance/debt"))}
      />
      <NavCard
        title="EMI & recurring"
        subtitle="Monthly charges; materialize into transactions"
        icon="repeat-outline"
        iconTint={c.accent}
        onPress={() => router.push(href("/(private)/(main)/finance/recurring"))}
      />
    </Screen>
  );
}
