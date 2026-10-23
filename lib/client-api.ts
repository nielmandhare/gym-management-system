"use client";

import { useCallback, useEffect, useState } from "react";

export class ClientApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public fields?: Record<string, string>,
  ) {
    super(message);
  }
}

/** Calls our own API and returns the `data` payload, or throws ClientApiError. */
export async function api<T>(url: string, init?: RequestInit): Promise<T> {
  let res: Response;
  try {
    res = await fetch(url, {
      ...init,
      headers: { "Content-Type": "application/json", ...init?.headers },
    });
  } catch {
    throw new ClientApiError("Cannot reach the server. Check your connection.", 0);
  }
  let body: { data?: T; error?: { message?: string; fields?: Record<string, string> } } = {};
  try {
    body = await res.json();
  } catch {
    // non-JSON response; handled below
  }
  if (!res.ok) {
    throw new ClientApiError(
      body.error?.message ?? "Something went wrong. Please try again.",
      res.status,
      body.error?.fields,
    );
  }
  return body.data as T;
}

type Result<T> = { key: string; data?: T; error?: string; status?: number };

/** GET hook with loading/error state and a manual reload. */
/** Pass null to skip the request (e.g. when the role may not call the API). */
export function useApi<T>(url: string | null) {
  const [tick, setTick] = useState(0);
  const [result, setResult] = useState<Result<T> | null>(null);
  const key = `${url}#${tick}`;

  useEffect(() => {
    if (!url) return;
    let cancelled = false;
    api<T>(url)
      .then((data) => !cancelled && setResult({ key, data }))
      .catch(
        (e: Error) =>
          !cancelled &&
          setResult({ key, error: e.message, status: e instanceof ClientApiError ? e.status : undefined }),
      );
    return () => {
      cancelled = true;
    };
  }, [url, key]);

  const reload = useCallback(() => setTick((t) => t + 1), []);
  const current = result?.key === key ? result : null;
  return {
    data: current?.data ?? result?.data,
    error: current?.error,
    status: current?.status,
    loading: current === null,
    reload,
  };
}
