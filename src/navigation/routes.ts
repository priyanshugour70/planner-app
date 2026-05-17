export const routes = {
  welcome: "/(public)/welcome",
  login: "/(public)/login",
  signup: "/(public)/signup",
  home: "/(private)/(main)",
  sessions: "/(private)/(main)/sessions",
  habits: "/(private)/(main)/habits",
  journal: "/(private)/(main)/journal",
  notes: "/(private)/(main)/notes",
  calendar: "/(private)/(main)/calendar",
} as const;
