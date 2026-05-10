/** `YYYY-MM-DD` in local calendar (not UTC shifted). */
function localYmd(d: Date): string {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

/** Local calendar month [first, last] as `YYYY-MM-DD`. */
export function localMonthRange(ref = new Date()): { from: string; to: string } {
  const y = ref.getFullYear();
  const m = ref.getMonth();
  const start = new Date(y, m, 1);
  const end = new Date(y, m + 1, 0);
  return { from: localYmd(start), to: localYmd(end) };
}

export function previousLocalMonthRange(ref = new Date()): { from: string; to: string } {
  const prev = new Date(ref.getFullYear(), ref.getMonth() - 1, 15);
  return localMonthRange(prev);
}
