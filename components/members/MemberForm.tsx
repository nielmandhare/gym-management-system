"use client";

import { useState } from "react";
import {
  MEMBERSHIP_PLANS,
  PLAN_DETAILS,
  addMonths,
  isValidDateString,
  todayISO,
} from "@/lib/business/membership";
import type { MembershipPlan } from "@/lib/business/membership";
import { ClientApiError } from "@/lib/client-api";
import { validateMemberInput } from "@/lib/validation/member";
import type { MemberInput } from "@/lib/validation/member";
import { Field, btnPrimary, btnSecondary, inputCls } from "@/components/ui/common";

type Props = {
  initial?: MemberInput;
  submitLabel: string;
  onSubmit: (values: MemberInput) => Promise<void>;
  onCancel: () => void;
};

function defaults(): MemberInput {
  const start = todayISO();
  return {
    full_name: "",
    email: "",
    phone: "",
    membership_plan: "Monthly",
    start_date: start,
    end_date: addMonths(start, PLAN_DETAILS.Monthly.months),
  };
}

export default function MemberForm({ initial, submitLabel, onSubmit, onCancel }: Props) {
  const [values, setValues] = useState<MemberInput>(() => initial ?? defaults());
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  function set<K extends keyof MemberInput>(key: K, value: MemberInput[K]) {
    setValues((v) => {
      const next = { ...v, [key]: value };
      // Convenience: changing plan or start date suggests a matching end date.
      if ((key === "membership_plan" || key === "start_date") && isValidDateString(next.start_date)) {
        next.end_date = addMonths(next.start_date, PLAN_DETAILS[next.membership_plan].months);
      }
      return next;
    });
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const result = validateMemberInput(values);
    if (!result.valid) {
      setErrors(result.errors);
      return;
    }
    setErrors({});
    setSaving(true);
    try {
      await onSubmit(result.value);
    } catch (err) {
      if (err instanceof ClientApiError && err.fields) setErrors(err.fields);
      else setErrors({ _form: err instanceof Error ? err.message : "Could not save member" });
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-4 rounded-lg border border-slate-200 bg-white p-5">
      {errors._form && (
        <div role="alert" className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-900">
          {errors._form}
        </div>
      )}
      <div className="grid gap-4 sm:grid-cols-2">
        <Field label="Full name" error={errors.full_name}>
          <input className={inputCls} name="full_name" value={values.full_name} onChange={(e) => set("full_name", e.target.value)} />
        </Field>
        <Field label="Email" error={errors.email}>
          <input className={inputCls} name="email" type="email" value={values.email} onChange={(e) => set("email", e.target.value)} />
        </Field>
        <Field label="Phone (10 digits)" error={errors.phone}>
          <input className={inputCls} name="phone" inputMode="numeric" value={values.phone} onChange={(e) => set("phone", e.target.value)} />
        </Field>
        <Field label="Membership plan" error={errors.membership_plan}>
          <select
            name="membership_plan"
            className={inputCls}
            value={values.membership_plan}
            onChange={(e) => set("membership_plan", e.target.value as MembershipPlan)}
          >
            {MEMBERSHIP_PLANS.map((p) => (
              <option key={p}>{p}</option>
            ))}
          </select>
        </Field>
        <Field label="Start date" error={errors.start_date}>
          <input className={inputCls} name="start_date" type="date" value={values.start_date} onChange={(e) => set("start_date", e.target.value)} />
        </Field>
        <Field label="End date" error={errors.end_date}>
          <input className={inputCls} name="end_date" type="date" value={values.end_date} onChange={(e) => set("end_date", e.target.value)} />
        </Field>
      </div>
      <div className="flex justify-end gap-2">
        <button type="button" className={btnSecondary} onClick={onCancel} disabled={saving}>
          Cancel
        </button>
        <button type="submit" className={btnPrimary} disabled={saving}>
          {saving ? "Saving..." : submitLabel}
        </button>
      </div>
    </form>
  );
}
