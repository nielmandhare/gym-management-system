"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import MemberForm from "@/components/members/MemberForm";
import {
  ConfirmDialog,
  ErrorBox,
  Loading,
  PageHeader,
  StatusBadge,
  btnDanger,
  btnSecondary,
} from "@/components/ui/common";
import { useToast } from "@/components/ui/Toast";
import { useAuth } from "@/components/auth/AuthProvider";
import { can } from "@/lib/auth/permissions";
import { ClientApiError, api, useApi } from "@/lib/client-api";
import type { Member } from "@/lib/services/members";

export default function MemberDetail({ id }: { id: string }) {
  const router = useRouter();
  const toast = useToast();
  const canWrite = can(useAuth().role, "members:write");
  const { data, error, loading, reload } = useApi<Member>(`/api/members/${id}`);
  const [editing, setEditing] = useState(false);
  const [confirming, setConfirming] = useState(false);
  const [deleting, setDeleting] = useState(false);

  async function handleDelete() {
    setDeleting(true);
    try {
      await api(`/api/members/${id}`, { method: "DELETE" });
      toast.success("Member deleted");
      router.push("/members");
    } catch (e) {
      toast.error(e instanceof ClientApiError ? e.message : "Could not delete member");
      setDeleting(false);
      setConfirming(false);
    }
  }

  if (error) {
    return (
      <div className="space-y-4">
        <ErrorBox message={error} onRetry={reload} />
        <Link href="/members" className="text-sm text-emerald-700 hover:underline">Back to members</Link>
      </div>
    );
  }
  if (loading || !data) return <Loading label="Loading member..." />;

  const rows: [string, React.ReactNode][] = [
    ["Email", data.email],
    ["Phone", data.phone],
    ["Plan", data.membership_plan],
    ["Start date", data.start_date],
    ["End date", data.end_date],
    ["Status", <StatusBadge key="s" status={data.status} />],
    ["Created", new Date(data.created_at).toLocaleString()],
    ["Member ID", <span key="i" className="font-mono text-xs">{data.id}</span>],
  ];

  return (
    <div className="max-w-3xl">
      <PageHeader
        title={data.full_name}
        subtitle="Member details"
        action={
          canWrite && !editing && (
            <div className="flex gap-2">
              <button className={btnSecondary} onClick={() => setEditing(true)}>Edit</button>
              <button className={btnDanger} onClick={() => setConfirming(true)}>Delete</button>
            </div>
          )
        }
      />
      {editing ? (
        <MemberForm
          initial={{
            full_name: data.full_name,
            email: data.email,
            phone: data.phone,
            membership_plan: data.membership_plan,
            start_date: data.start_date,
            end_date: data.end_date,
          }}
          submitLabel="Save changes"
          onCancel={() => setEditing(false)}
          onSubmit={async (values) => {
            await api<Member>(`/api/members/${id}`, { method: "PATCH", body: JSON.stringify(values) });
            toast.success("Member updated");
            setEditing(false);
            reload();
          }}
        />
      ) : (
        <dl className="divide-y divide-slate-100 rounded-lg border border-slate-200 bg-white">
          {rows.map(([label, value]) => (
            <div key={label} className="flex flex-col gap-1 px-4 py-3 text-sm sm:flex-row">
              <dt className="w-40 text-slate-600">{label}</dt>
              <dd className="font-medium">{value}</dd>
            </div>
          ))}
        </dl>
      )}
      <Link href="/members" className="mt-4 inline-block text-sm text-emerald-700 hover:underline">
        Back to members
      </Link>
      {confirming && (
        <ConfirmDialog
          title="Delete member?"
          message={`${data.full_name} will be permanently removed. This cannot be undone.`}
          busy={deleting}
          onConfirm={handleDelete}
          onCancel={() => setConfirming(false)}
        />
      )}
    </div>
  );
}
