"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { createContext, useContext, useEffect } from "react";
import { ErrorBox, Loading, btnPrimary } from "@/components/ui/common";
import { can, homePath } from "@/lib/auth/permissions";
import type { Action } from "@/lib/auth/permissions";
import type { AuthUser } from "@/lib/auth/session";
import { useApi } from "@/lib/client-api";

const AuthContext = createContext<AuthUser | null>(null);

export function useAuth(): AuthUser {
  const user = useContext(AuthContext);
  if (!user) throw new Error("useAuth must be used inside <AuthProvider>");
  return user;
}

/** Loads the logged-in user once; sends visitors without a session to /login. */
export function AuthProvider({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const { data, error, status, loading, reload } = useApi<AuthUser>("/api/auth/me");
  const unauthenticated = status === 401;

  useEffect(() => {
    if (unauthenticated) router.replace("/login");
  }, [unauthenticated, router]);

  if (unauthenticated) return <Loading label="Redirecting to login..." />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;
  if (loading || !data) return <Loading label="Loading..." />;
  return <AuthContext.Provider value={data}>{children}</AuthContext.Provider>;
}

/** Shows the page only if the user's role allows the action. UI convenience only; the API enforces access. */
export function RoleGuard({ action, children }: { action: Action; children: React.ReactNode }) {
  const user = useAuth();
  if (can(user.role, action)) return <>{children}</>;
  return (
    <div role="alert" className="max-w-md rounded-lg border border-slate-200 bg-white p-6">
      <h1 className="text-lg font-semibold">Access denied</h1>
      <p className="mt-1 text-sm text-slate-600">
        Your account ({user.role}) cannot open this page.
      </p>
      <Link href={homePath(user.role)} className={`${btnPrimary} mt-4`}>
        Go to my home page
      </Link>
    </div>
  );
}
