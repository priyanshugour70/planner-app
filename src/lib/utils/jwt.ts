/** Decode JWT payload without verification — client-side hints only. */
export function decodeJwtPayload<T extends Record<string, unknown> = Record<string, unknown>>(
  token: string
): T | null {
  const parts = token.split(".");
  if (parts.length < 2) return null;
  try {
    const b64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const padded = b64 + "===".slice((b64.length + 3) % 4);
    const json = globalThis.atob(padded);
    return JSON.parse(json) as T;
  } catch {
    return null;
  }
}

export function jwtExpiresAtMs(token: string): number | null {
  const p = decodeJwtPayload<{ exp?: number }>(token);
  if (!p?.exp || typeof p.exp !== "number") return null;
  return p.exp * 1000;
}
