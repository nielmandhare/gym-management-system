"use client";

import { createContext, useCallback, useContext, useState } from "react";

type Toast = { id: number; kind: "success" | "error"; message: string };
const ToastContext = createContext<(kind: Toast["kind"], message: string) => void>(() => {});

export function useToast() {
  const push = useContext(ToastContext);
  return {
    success: (m: string) => push("success", m),
    error: (m: string) => push("error", m),
  };
}

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const push = useCallback((kind: Toast["kind"], message: string) => {
    const id = Date.now() + Math.random();
    setToasts((t) => [...t, { id, kind, message }]);
    setTimeout(() => setToasts((t) => t.filter((x) => x.id !== id)), 4000);
  }, []);

  return (
    <ToastContext.Provider value={push}>
      {children}
      <div className="fixed right-4 top-4 z-50 flex w-80 max-w-[calc(100vw-2rem)] flex-col gap-2" aria-live="polite">
        {toasts.map((t) => (
          <div
            key={t.id}
            role={t.kind === "error" ? "alert" : "status"}
            className={`rounded-md border px-4 py-3 text-sm shadow ${
              t.kind === "success"
                ? "border-emerald-200 bg-emerald-50 text-emerald-900"
                : "border-red-200 bg-red-50 text-red-900"
            }`}
          >
            {t.message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}
