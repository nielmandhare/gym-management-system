"use client";

import { useState, useSyncExternalStore } from "react";
import { usePwa } from "@/components/pwa/PwaProvider";

const subscribeHydration = () => () => {};
const clientReady = () => true;
const serverReady = () => false;

/**
 * "Install app" button. Shown only when the browser offers installation, or on
 * iOS (where it explains the Share-menu steps). Hidden once installed.
 */
export default function InstallButton({ className = "" }: { className?: string }) {
  const { canInstall, installed, isIos, install } = usePwa();
  const [showIosHelp, setShowIosHelp] = useState(false);
  const hydrated = useSyncExternalStore(subscribeHydration, clientReady, serverReady);

  // The install event can arrive before this streamed boundary hydrates.
  if (!hydrated || installed) return null;
  if (!canInstall && !isIos) return null;

  const base =
    "inline-flex w-full items-center justify-center gap-2 rounded-md border border-emerald-600 bg-white px-3 py-2 text-sm font-medium text-emerald-700 hover:bg-emerald-50";

  return (
    <div className={className}>
      <button
        type="button"
        className={base}
        onClick={canInstall ? install : () => setShowIosHelp((v) => !v)}
      >
        <svg aria-hidden="true" viewBox="0 0 20 20" className="h-4 w-4 fill-current">
          <path d="M10 2a1 1 0 0 1 1 1v7.59l2.3-2.3a1 1 0 1 1 1.4 1.42l-4 4a1 1 0 0 1-1.4 0l-4-4a1 1 0 1 1 1.4-1.42L9 10.59V3a1 1 0 0 1 1-1Zm-6 13a1 1 0 0 1 1 1v1h10v-1a1 1 0 1 1 2 0v1a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-1a1 1 0 0 1 1-1Z" />
        </svg>
        Install app
      </button>
      {showIosHelp && (
        <p role="status" className="mt-2 rounded-md bg-slate-100 p-2 text-xs text-slate-700">
          On iPhone or iPad: tap the Share button in Safari, then choose &quot;Add to Home Screen&quot;.
        </p>
      )}
    </div>
  );
}
