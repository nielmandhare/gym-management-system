"use client";

import { useState } from "react";
import {
  ConfirmDialog,
  EmptyState,
  ErrorBox,
  Field,
  Loading,
  PageHeader,
  btnDanger,
  btnPrimary,
  btnSecondary,
  inputCls,
} from "@/components/ui/common";
import { useToast } from "@/components/ui/Toast";
import { ClientApiError, api, useApi } from "@/lib/client-api";
import type { Trainer } from "@/lib/services/trainers";
import { validateTrainerInput } from "@/lib/validation/trainer";

const empty = { full_name: "", email: "", phone: "", specialization: "" };

export default function TrainerManager() {
  const toast = useToast();
  const { data, error, loading, reload } = useApi<Trainer[]>("/api/trainers");
  const [showForm, setShowForm] = useState(false);
  const [values, setValues] = useState(empty);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);
  const [toDelete, setToDelete] = useState<Trainer | null>(null);
  const [deleting, setDeleting] = useState(false);

  async function handleAdd(e: React.FormEvent) {
    e.preventDefault();
    const result = validateTrainerInput(values);
    if (!result.valid) return setErrors(result.errors);
    setErrors({});
    setSaving(true);
    try {
      await api("/api/trainers", { method: "POST", body: JSON.stringify(result.value) });
      toast.success("Trainer added");
      setValues(empty);
      setShowForm(false);
      reload();
    } catch (err) {
      if (err instanceof ClientApiError && err.fields) setErrors(err.fields);
      else setErrors({ _form: err instanceof Error ? err.message : "Could not add trainer" });
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!toDelete) return;
    setDeleting(true);
    try {
      await api(`/api/trainers/${toDelete.id}`, { method: "DELETE" });
      toast.success("Trainer deleted");
      reload();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Could not delete trainer");
    } finally {
      setDeleting(false);
      setToDelete(null);
    }
  }

  const fields: [keyof typeof empty, string][] = [
    ["full_name", "Full name"],
    ["email", "Email"],
    ["phone", "Phone (10 digits)"],
    ["specialization", "Specialization"],
  ];

  return (
    <div>
      <PageHeader
        title="Trainers"
        subtitle="Gym trainers and their specializations"
        action={!showForm && <button className={btnPrimary} onClick={() => setShowForm(true)}>Add trainer</button>}
      />
      {showForm && (
        <form onSubmit={handleAdd} noValidate className="mb-6 space-y-4 rounded-lg border border-slate-200 bg-white p-5">
          {errors._form && <ErrorBox message={errors._form} />}
          <div className="grid gap-4 sm:grid-cols-2">
            {fields.map(([key, label]) => (
              <Field key={key} label={label} error={errors[key]}>
                <input
                  className={inputCls}
                  name={key}
                  value={values[key]}
                  onChange={(e) => setValues({ ...values, [key]: e.target.value })}
                />
              </Field>
            ))}
          </div>
          <div className="flex justify-end gap-2">
            <button type="button" className={btnSecondary} disabled={saving} onClick={() => { setShowForm(false); setErrors({}); }}>
              Cancel
            </button>
            <button type="submit" className={btnPrimary} disabled={saving}>
              {saving ? "Saving..." : "Add trainer"}
            </button>
          </div>
        </form>
      )}
      {error ? (
        <ErrorBox message={error} onRetry={reload} />
      ) : loading ? (
        <Loading label="Loading trainers..." />
      ) : data && data.length === 0 ? (
        <EmptyState title="No trainers yet" hint="Add a trainer to see them listed here." />
      ) : (
        <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-600">
              <tr>
                <th className="px-4 py-3">Name</th>
                <th className="px-4 py-3">Email</th>
                <th className="px-4 py-3">Phone</th>
                <th className="px-4 py-3">Specialization</th>
                <th className="px-4 py-3"><span className="sr-only">Actions</span></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {data?.map((t) => (
                <tr key={t.id}>
                  <td className="px-4 py-3 font-medium">{t.full_name}</td>
                  <td className="px-4 py-3">{t.email}</td>
                  <td className="px-4 py-3">{t.phone}</td>
                  <td className="px-4 py-3">{t.specialization}</td>
                  <td className="px-4 py-3 text-right">
                    <button className={`${btnDanger} !px-3 !py-1`} onClick={() => setToDelete(t)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {toDelete && (
        <ConfirmDialog
          title="Delete trainer?"
          message={`${toDelete.full_name} will be permanently removed.`}
          busy={deleting}
          onConfirm={handleDelete}
          onCancel={() => setToDelete(null)}
        />
      )}
    </div>
  );
}
