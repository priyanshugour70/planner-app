export type AuthUserPublic = {
  id: string;
  username: string;
  email: string;
  emailVerified: boolean;
  accountStatus: string;
};

export type AuthSessionPayload = {
  accessToken: string;
  refreshToken?: string;
  expiresIn: number;
  tokenType: "Bearer";
  sessionId: string;
  user: AuthUserPublic;
};

export type MeResponse = {
  id: string;
  username: string;
  email: string;
  emailVerified: boolean;
  accountStatus: string;
  lastLoginAt: string | null;
  profile: {
    fullName: string | null;
    firstName: string | null;
    lastName: string | null;
  };
  roles: string[];
};

export type AuthSessionRow = {
  id: string;
  deviceType: string | null;
  platform: string | null;
  browser: string | null;
  os: string | null;
  ipAddress: string | null;
  lastActivityAt: string;
  createdAt: string;
  revoked: boolean;
};
