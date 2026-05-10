/** In-memory access token — never persisted to disk. */
let accessToken: string | null = null;

export const tokenMemory = {
  get(): string | null {
    return accessToken;
  },
  set(token: string | null): void {
    accessToken = token;
  },
};
