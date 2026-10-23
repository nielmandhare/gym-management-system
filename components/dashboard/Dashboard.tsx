"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { useAuth } from "@/components/auth/AuthProvider";
import { can } from "@/lib/auth/permissions";
import { EmptyState, ErrorBox, Loading, PageHeader, StatusBadge, btnPrimary } from "@/components/ui/common";
import { useApi } from "@/lib/client-api";
import type { Member } from "@/lib/services/members";
import type { Trainer } from "@/lib/services/trainers";

function Card({ label, value }: { label: string; value: number }) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-5">
      <p className="text-sm text-slate-600">{label}</p>
      <p className="mt-1 text-3xl font-semibold text-slate-900">{value}</p>
    </div>
  );
}

export default function Dashboard() {
  const user = useAuth();
  const router = useRouter();
  const allowed = can(user.role, "dashboard:read");
  useEffect(() => {
    if (!allowed) router.replace("/me"); // customers have no dashboard
  }, [allowed, router]);
  const members = useApi<Member[]>(allowed ? "/api/members" : null);
  const trainers = useApi<Trainer[]>(allowed ? "/api/trainers" : null);

  const error = members.error ?? trainers.error;
  const reload = () => {
    members.reload();
    trainers.reload();
  };

  if (!allowed) return <Loading label="Redirecting..." />;

  return (
    <div>
      <PageHeader
        title="Dashboard"
        subtitle="Overview of your gym"
        action={can(user.role, "members:write") && <Link href="/members/new" className={btnPrimary}>Add member</Link>}
      />
      {error ? (
        <ErrorBox message={error} onRetry={reload} />
      ) : members.loading || trainers.loading || !members.data || !trainers.data ? (
        <Loading label="Loading dashboard..." />
      ) : (
        <>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Card label="Total members" value={members.data.length} />
            <Card label="Active members" value={members.data.filter((m) => m.status === "Active").length} />
            <Card label="Expired memberships" value={members.data.filter((m) => m.status === "Expired").length} />
            <Card label="Total trainers" value={trainers.data.length} />
          </div>
          <h2 className="mb-3 mt-8 text-lg font-semibold">Recent members</h2>
          {members.data.length === 0 ? (
            <EmptyState title="No members yet" hint="Add your first member to see them here." />
          ) : (
            <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-slate-600">
                  <tr>
                    <th className="px-4 py-3">Name</th>
                    <th className="px-4 py-3">Plan</th>
                    <th className="px-4 py-3">End date</th>
                    <th className="px-4 py-3">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {members.data.slice(0, 5).map((m) => (
                    <tr key={m.id}>
                      <td className="px-4 py-3 font-medium">
                        <Link href={`/members/${m.id}`} className="text-emerald-700 hover:underline">{m.full_name}</Link>
                      </td>
                      <td className="px-4 py-3">{m.membership_plan}</td>
                      <td className="px-4 py-3">{m.end_date}</td>
                      <td className="px-4 py-3"><StatusBadge status={m.status} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </div>
  );
}
