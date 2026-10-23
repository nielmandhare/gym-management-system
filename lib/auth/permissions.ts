// Pure role/permission rules. No I/O, so they are easy to test.

export const ROLES = ["admin", "trainer", "customer"] as const;
export type Role = (typeof ROLES)[number];

export type Action =
  | "dashboard:read"
  | "members:read"
  | "members:write"
  | "trainers:read"
  | "trainers:write"
  | "users:manage"
  | "me:read";

const PERMISSIONS: Record<Role, readonly Action[]> = {
  admin: [
    "dashboard:read",
    "members:read",
    "members:write",
    "trainers:read",
    "trainers:write",
    "users:manage",
    "me:read",
  ],
  trainer: ["dashboard:read", "members:read", "trainers:read", "me:read"],
  customer: ["me:read"],
};

export function isValidRole(role: unknown): role is Role {
  return typeof role === "string" && (ROLES as readonly string[]).includes(role);
}

/** Unknown roles get no permissions. */
export function can(role: unknown, action: Action): boolean {
  return isValidRole(role) && PERMISSIONS[role].includes(action);
}

/** Page each role lands on after login. */
export function homePath(role: Role): string {
  return role === "customer" ? "/me" : "/";
}

/** Navigation links visible to a role. */
export function navLinks(role: Role): { href: string; label: string }[] {
  const links = [
    { href: "/", label: "Dashboard", action: "dashboard:read" as Action },
    { href: "/members", label: "Members", action: "members:read" as Action },
    { href: "/trainers", label: "Trainers", action: "trainers:write" as Action },
    { href: "/users", label: "Users", action: "users:manage" as Action },
    { href: "/me", label: role === "customer" ? "My Membership" : "My Profile", action: "me:read" as Action },
  ];
  const visible = links.filter((l) => can(role, l.action));
  return [...visible, { href: "/plans", label: "Plans" }];
}
