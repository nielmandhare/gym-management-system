"use client";

import { useState } from "react";
import { useAuth } from "@/components/auth/AuthProvider";
import {
  EmptyState, ErrorBox, Field, Loading, PageHeader, btnPrimary, btnSecondary, inputCls,
} from "@/components/ui/common";
import { useToast } from "@/components/ui/Toast";
import { ROLES } from "@/lib/auth/permissions";
import type { Role } from "@/lib/auth/permissions";
import { ClientApiError, api, useApi } from "@/lib/client-api";
import type { Profile } from "@/lib/services/users";
import { validateCreateUser } from "@/lib/validation/auth";

const empty = { full_name: "", email: "", password: "", role: "trainer" as Role };

export default function UserManager() {
  const me = useAuth();
  const toast = useToast();
  const { data, error, loading, reload } = useApi<Profile[]>("/api/users");
  const [showForm, setShowForm] = useState(false);
  const [values, setValues] = useState(empty);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  async function create(e: React.FormEvent) {
    e.preventDefault();
    const check = validateCreateUser(values);
    if (!check.valid) return setErrors(check.errors);
    setErrors({});
    setSaving(true);
    try {
      await api("/api/users", { method: "POST", body: JSON.stringify(check.value) });
      toast.success("User created");
      setValues(empty);
      setShowForm(false);
      reload();
    } catch (err) {
      setErrors(
        err instanceof ClientApiError && err.fields
          ? err.fields
          : { _form: err instanceof Error ? err.message : "Could not create user" },
      );
    } finally {
      setSaving(false);
    }
  }

  async function changeRole(id: string, role: Role) {
    try {
      await api(`/api/users/${id}`, { method: "PATCH", body: JSON.stringify({ role }) });
      toast.success("Role updated");
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Could not update role");
    }
    reload();
  }

  return (
    <div>
      <PageHeader
        title="Users"
        subtitle="Accounts and their roles. Link a customer or trainer to gym records by using the same email."
        action={!showForm && <button className={btnPrimary} onClick={() => setShowForm(true)}>Create user</button>}
      />
      {showForm && (
        <form onSubmit={create} noValidate className="mb-6 space-y-4 rounded-lg border border-slate-200 bg-white p-5">
          {errors._form && <ErrorBox message={errors._form} />}
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Full name" error={errors.full_name}>
              <input className={inputCls} name="full_name" value={values.full_name} onChange={(e) => setValues({ ...values, full_name: e.target.value })} />
            </Field>
            <Field label="Email" error={errors.email}>
              <input className={inputCls} name="email" type="email" value={values.email} onChange={(e) => setValues({ ...values, email: e.target.value })} />
            </Field>
            <Field label="Temporary password (8 to 72 characters)" error={errors.password}>
              <input className={inputCls} name="password" type="password" autoComplete="new-password" value={values.password} onChange={(e) => setValues({ ...values, password: e.target.value })} />
            </Field>
            <Field label="Role" error={errors.role}>
              <select name="role" className={inputCls} value={values.role} onChange={(e) => setValues({ ...values, role: e.target.value as Role })}>
                {ROLES.map((r) => <option key={r}>{r}</option>)}
              </select>
            </Field>
          </div>
          <div className="flex justify-end gap-2">
            <button type="button" className={btnSecondary} disabled={saving} onClick={() => { setShowForm(false); setErrors({}); }}>Cancel</button>
            <button type="submit" className={btnPrimary} disabled={saving}>{saving ? "Creating..." : "Create user"}</button>
          </div>
        </form>
      )}
      {error ? (
        <ErrorBox message={error} onRetry={reload} />
      ) : loading ? (
        <Loading label="Loading users..." />
      ) : data && data.length === 0 ? (
        <EmptyState title="No users" />
      ) : (
        <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-600">
              <tr>
                <th className="px-4 py-3">Name</th>
                <th className="px-4 py-3">Email</th>
                <th className="px-4 py-3">Role</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {data?.map((u) => (
                <tr key={u.id}>
                  <td className="px-4 py-3 font-medium">{u.full_name || "-"}</td>
                  <td className="px-4 py-3">{u.email}</td>
                  <td className="px-4 py-3">
                    {u.id === me.id ? (
                      <span className="capitalize">{u.role} (you)</span>
                    ) : (
                      <select
                        aria-label={`Role for ${u.email}`}
                        className={`${inputCls} !w-auto`}
                        value={u.role}
                        onChange={(e) => changeRole(u.id, e.target.value as Role)}
                      >
                        {ROLES.map((r) => <option key={r}>{r}</option>)}
                      </select>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
