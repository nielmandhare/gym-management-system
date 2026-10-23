"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { EmptyState, ErrorBox, Loading, PageHeader, StatusBadge, btnPrimary, inputCls } from "@/components/ui/common";
import { useAuth } from "@/components/auth/AuthProvider";
import { can } from "@/lib/auth/permissions";
import { useApi } from "@/lib/client-api";
import type { Member } from "@/lib/services/members";

export default function MemberList() {
  const canWrite = can(useAuth().role, "members:write");
  const [search, setSearch] = useState("");
  const [query, setQuery] = useState("");

  // Debounce typing so we do not call the API on every keystroke.
  useEffect(() => {
    const t = setTimeout(() => setQuery(search.trim()), 300);
    return () => clearTimeout(t);
  }, [search]);

  const { data, error, loading, reload } = useApi<Member[]>(`/api/members?q=${encodeURIComponent(query)}`);

  return (
    <div>
      <PageHeader
        title="Members"
        subtitle="Search, view, edit or remove gym members"
        action={
          canWrite && (
            <Link href="/members/new" className={btnPrimary}>
              Add member
            </Link>
          )
        }
      />
      <input
        className={`${inputCls} mb-4 max-w-md`}
        placeholder="Search by name or email"
        aria-label="Search members"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />
      {error ? (
        <ErrorBox message={error} onRetry={reload} />
      ) : loading ? (
        <Loading label="Loading members..." />
      ) : data && data.length === 0 ? (
        <EmptyState
          title={query ? "No members match your search" : "No members yet"}
          hint={query ? "Try a different name or email." : "Add your first member to get started."}
          action={!query && canWrite && <Link href="/members/new" className={btnPrimary}>Add member</Link>}
        />
      ) : (
        <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-600">
              <tr>
                <th className="px-4 py-3">Name</th>
                <th className="px-4 py-3">Email</th>
                <th className="px-4 py-3">Plan</th>
                <th className="px-4 py-3">End date</th>
                <th className="px-4 py-3">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {data?.map((m) => (
                <tr key={m.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-medium">
                    <Link href={`/members/${m.id}`} className="text-emerald-700 hover:underline">
                      {m.full_name}
                    </Link>
                  </td>
                  <td className="px-4 py-3">{m.email}</td>
                  <td className="px-4 py-3">{m.membership_plan}</td>
                  <td className="px-4 py-3">{m.end_date}</td>
                  <td className="px-4 py-3"><StatusBadge status={m.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
