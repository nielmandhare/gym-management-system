"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, useSyncExternalStore } from "react";
import { Field, btnPrimary, inputCls } from "@/components/ui/common";
import { useToast } from "@/components/ui/Toast";
import { homePath } from "@/lib/auth/permissions";
import type { AuthUser } from "@/lib/auth/session";
import { ClientApiError, api } from "@/lib/client-api";
import { validateLogin, validateRegister } from "@/lib/validation/auth";

const subscribeHydration = () => () => {};
const clientReady = () => true;
const serverReady = () => false;

function Card({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="w-full max-w-sm rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <h1 className="text-xl font-semibold text-emerald-700">GymManager</h1>
      <h2 className="mb-4 mt-1 text-sm text-slate-600">{title}</h2>
      {children}
    </div>
  );
}

function useAuthForm() {
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [busy, setBusy] = useState(false);
  const hydrated = useSyncExternalStore(subscribeHydration, clientReady, serverReady);
  return { errors, setErrors, busy, setBusy, hydrated };
}

export function LoginForm() {
  const router = useRouter();
  const toast = useToast();
  const [values, setValues] = useState({ email: "", password: "" });
  const { errors, setErrors, busy, setBusy, hydrated } = useAuthForm();

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    const check = validateLogin(values);
    if (!check.valid) return setErrors(check.errors);
    setErrors({});
    setBusy(true);
    try {
      const user = await api<AuthUser>("/api/auth/login", { method: "POST", body: JSON.stringify(values) });
      toast.success(`Welcome, ${user.full_name || user.email}`);
      router.replace(homePath(user.role));
    } catch (err) {
      setErrors(
        err instanceof ClientApiError && err.fields
          ? err.fields
          : { _form: err instanceof Error ? err.message : "Could not log in" },
      );
      setBusy(false);
    }
  }

  return (
    <Card title="Log in to your account">
      <form onSubmit={submit} noValidate className="space-y-4">
        {errors._form && (
          <div role="alert" className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-900">
            {errors._form}
          </div>
        )}
        <Field label="Email" error={errors.email}>
          <input className={inputCls} name="email" type="email" autoComplete="email" value={values.email}
            onChange={(e) => setValues({ ...values, email: e.target.value })} />
        </Field>
        <Field label="Password" error={errors.password}>
          <input className={inputCls} name="password" type="password" autoComplete="current-password" value={values.password}
            onChange={(e) => setValues({ ...values, password: e.target.value })} />
        </Field>
        <button type="submit" className={`${btnPrimary} w-full`} disabled={!hydrated || busy}>
          {busy ? "Logging in..." : "Log in"}
        </button>
      </form>
      <p className="mt-4 text-sm text-slate-600">
        New member? <Link href="/register" className="text-emerald-700 hover:underline">Create an account</Link>
      </p>
    </Card>
  );
}

export function RegisterForm() {
  const router = useRouter();
  const toast = useToast();
  const [values, setValues] = useState({ full_name: "", email: "", password: "" });
  const [notice, setNotice] = useState("");
  const { errors, setErrors, busy, setBusy, hydrated } = useAuthForm();

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    const check = validateRegister(values);
    if (!check.valid) return setErrors(check.errors);
    setErrors({});
    setBusy(true);
    try {
      const res = await api<{ user: AuthUser | null; needs_confirmation: boolean }>("/api/auth/register", {
        method: "POST",
        body: JSON.stringify(values),
      });
      if (res.user) {
        toast.success("Account created");
        router.replace(homePath(res.user.role));
        return;
      }
      setNotice("Account created. Check your email to confirm it, then log in.");
    } catch (err) {
      setErrors(
        err instanceof ClientApiError && err.fields
          ? err.fields
          : { _form: err instanceof Error ? err.message : "Could not register" },
      );
    }
    setBusy(false);
  }

  return (
    <Card title="Create a customer account">
      {notice ? (
        <p role="status" className="rounded-md border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-900">
          {notice}
        </p>
      ) : (
        <form onSubmit={submit} noValidate className="space-y-4">
          {errors._form && (
            <div role="alert" className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-900">
              {errors._form}
            </div>
          )}
          <Field label="Full name" error={errors.full_name}>
            <input className={inputCls} name="full_name" value={values.full_name} autoComplete="name"
              onChange={(e) => setValues({ ...values, full_name: e.target.value })} />
          </Field>
          <Field label="Email" error={errors.email}>
            <input className={inputCls} name="email" type="email" autoComplete="email" value={values.email}
              onChange={(e) => setValues({ ...values, email: e.target.value })} />
          </Field>
          <Field label="Password (8 to 72 characters)" error={errors.password}>
            <input className={inputCls} name="password" type="password" autoComplete="new-password" value={values.password}
              onChange={(e) => setValues({ ...values, password: e.target.value })} />
          </Field>
          <button type="submit" className={`${btnPrimary} w-full`} disabled={!hydrated || busy}>
            {busy ? "Creating..." : "Register"}
          </button>
        </form>
      )}
      <p className="mt-4 text-sm text-slate-600">
        Already registered? <Link href="/login" className="text-emerald-700 hover:underline">Log in</Link>
      </p>
    </Card>
  );
}
