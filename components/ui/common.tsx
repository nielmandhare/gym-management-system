import type { MembershipStatus } from "@/lib/business/membership";

export const btnPrimary =
  "inline-flex items-center justify-center rounded-md bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60";
export const btnSecondary =
  "inline-flex items-center justify-center rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-60";
export const btnDanger =
  "inline-flex items-center justify-center rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-60";
export const inputCls =
  "w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:border-emerald-600 focus:outline-none focus:ring-1 focus:ring-emerald-600";

export function PageHeader({
  title,
  subtitle,
  action,
}: {
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
}) {
  return (
    <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">{title}</h1>
        {subtitle && <p className="text-sm text-slate-600">{subtitle}</p>}
      </div>
      {action}
    </div>
  );
}

export function Loading({ label = "Loading..." }: { label?: string }) {
  return (
    <div className="flex items-center gap-3 py-10 text-sm text-slate-600" role="status">
      <span className="h-5 w-5 animate-spin rounded-full border-2 border-emerald-600 border-t-transparent" />
      {label}
    </div>
  );
}

export function EmptyState({ title, hint, action }: { title: string; hint?: string; action?: React.ReactNode }) {
  return (
    <div className="rounded-lg border border-dashed border-slate-300 bg-white py-12 text-center">
      <p className="font-medium text-slate-800">{title}</p>
      {hint && <p className="mt-1 text-sm text-slate-600">{hint}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}

export function ErrorBox({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div role="alert" className="rounded-md border border-red-200 bg-red-50 p-4 text-sm text-red-900">
      {message}
      {onRetry && (
        <button onClick={onRetry} className="ml-3 font-medium underline">
          Retry
        </button>
      )}
    </div>
  );
}

export function StatusBadge({ status }: { status: MembershipStatus }) {
  const cls =
    status === "Active"
      ? "bg-emerald-100 text-emerald-800"
      : "bg-slate-200 text-slate-700";
  return <span className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${cls}`}>{status}</span>;
}

export function ConfirmDialog({
  title,
  message,
  confirmLabel = "Delete",
  busy,
  onConfirm,
  onCancel,
}: {
  title: string;
  message: string;
  confirmLabel?: string;
  busy?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}) {
  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/40 p-4" role="dialog" aria-modal="true">
      <div className="w-full max-w-sm rounded-lg bg-white p-6 shadow-lg">
        <h2 className="text-lg font-semibold text-slate-900">{title}</h2>
        <p className="mt-2 text-sm text-slate-600">{message}</p>
        <div className="mt-5 flex justify-end gap-2">
          <button className={btnSecondary} onClick={onCancel} disabled={busy}>
            Cancel
          </button>
          <button className={btnDanger} onClick={onConfirm} disabled={busy}>
            {busy ? "Deleting..." : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}

export function Field({
  label,
  error,
  children,
}: {
  label: string;
  error?: string;
  children: React.ReactNode;
}) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block font-medium text-slate-700">{label}</span>
      {children}
      {error && <span className="mt-1 block text-xs text-red-700">{error}</span>}
    </label>
  );
}
