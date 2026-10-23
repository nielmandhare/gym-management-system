"use client";

import Link from "next/link";
import { useAuth } from "@/components/auth/AuthProvider";
import { EmptyState, ErrorBox, Loading, PageHeader, StatusBadge } from "@/components/ui/common";
import { useApi } from "@/lib/client-api";
import type { Member } from "@/lib/services/members";
import type { Trainer } from "@/lib/services/trainers";

type MeData = { member: Member | null; trainer: Trainer | null };

function Rows({ rows }: { rows: [string, React.ReactNode][] }) {
  return (
    <dl className="max-w-2xl divide-y divide-slate-100 rounded-lg border border-slate-200 bg-white">
      {rows.map(([label, value]) => (
        <div key={label} className="flex flex-col gap-1 px-4 py-3 text-sm sm:flex-row">
          <dt className="w-40 text-slate-600">{label}</dt>
          <dd className="font-medium">{value}</dd>
        </div>
      ))}
    </dl>
  );
}

/** Customer: own membership. Trainer: own trainer record. Admin: account details. */
export default function MyProfile() {
  const user = useAuth();
  const { data, error, loading, reload } = useApi<MeData>("/api/me");

  const title = user.role === "customer" ? "My Membership" : "My Profile";
  if (error) return <ErrorBox message={error} onRetry={reload} />;
  if (loading || !data) return <Loading />;

  const account: [string, React.ReactNode][] = [
    ["Name", user.full_name || "-"],
    ["Email", user.email],
    ["Role", <span key="r" className="capitalize">{user.role}</span>],
  ];

  return (
    <div>
      <PageHeader title={title} subtitle={`Signed in as ${user.email}`} />
      {user.role === "customer" &&
        (data.member ? (
          <Rows
            rows={[
              ["Name", data.member.full_name],
              ["Plan", data.member.membership_plan],
              ["Start date", data.member.start_date],
              ["End date", data.member.end_date],
              ["Status", <StatusBadge key="s" status={data.member.status} />],
              ["Phone", data.member.phone],
            ]}
          />
        ) : (
          <EmptyState
            title="No membership found for your email"
            hint={`Ask the gym front desk to register a membership using ${user.email}.`}
            action={<Link href="/plans" className="text-sm text-emerald-700 hover:underline">View plans</Link>}
          />
        ))}
      {user.role === "trainer" &&
        (data.trainer ? (
          <Rows
            rows={[
              ["Name", data.trainer.full_name],
              ["Specialization", data.trainer.specialization],
              ["Phone", data.trainer.phone],
              ["Email", data.trainer.email],
            ]}
          />
        ) : (
          <EmptyState
            title="No trainer record found for your email"
            hint={`Ask an administrator to add a trainer with the email ${user.email}.`}
          />
        ))}
      {user.role === "admin" && <Rows rows={account} />}
    </div>
  );
}
