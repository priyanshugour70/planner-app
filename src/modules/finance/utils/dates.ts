export function isoDate(d: Date): string {
  return d.toISOString().slice(0, 10);
}

export function currentMonthRange(): { periodStart: string; periodEnd: string } {
  const now = new Date();
  const y = now.getFullYear();
  const m = now.getMonth();
  const start = new Date(y, m, 1);
  const end = new Date(y, m + 1, 0);
  return { periodStart: isoDate(start), periodEnd: isoDate(end) };
}

export function todayISO(): string {
  return isoDate(new Date());
}
