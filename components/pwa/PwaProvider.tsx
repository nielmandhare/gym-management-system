"use client";

import { createContext, useContext, useEffect, useState, useSyncExternalStore } from "react";

// The (non-standard) event Chromium-based browsers fire when the app is installable.
type InstallPromptEvent = Event & {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: "accepted" | "dismissed" }>;
};

type PwaState = {
  /** A native install prompt is available (Chrome, Edge, Android browsers). */
  canInstall: boolean;
  /** Running as an installed app already. */
  installed: boolean;
  /** iOS Safari has no install prompt; users add the app from the Share menu. */
  isIos: boolean;
  install: () => Promise<void>;
};

const PwaContext = createContext<PwaState>({
  canInstall: false,
  installed: false,
  isIos: false,
  install: async () => {},
});

export const usePwa = () => useContext(PwaContext);

function subscribeStandalone(onChange: () => void) {
  const mq = window.matchMedia("(display-mode: standalone)");
  mq.addEventListener("change", onChange);
  return () => mq.removeEventListener("change", onChange);
}
const standaloneNow = () =>
  window.matchMedia("(display-mode: standalone)").matches ||
  (navigator as Navigator & { standalone?: boolean }).standalone === true;

const iosNow = () => /iphone|ipad|ipod/i.test(navigator.userAgent);
const noSubscribe = () => () => {};

/** Registers the service worker and exposes the install prompt to the UI. */
export default function PwaProvider({ children }: { children: React.ReactNode }) {
  const [promptEvent, setPromptEvent] = useState<InstallPromptEvent | null>(null);
  const [justInstalled, setJustInstalled] = useState(false);
  const standalone = useSyncExternalStore(subscribeStandalone, standaloneNow, () => false);
  const isIos = useSyncExternalStore(noSubscribe, iosNow, () => false);

  useEffect(() => {
    if ("serviceWorker" in navigator) {
      navigator.serviceWorker.register("/sw.js", { scope: "/", updateViaCache: "none" }).catch(() => {
        // Registration can fail (private mode, blocked): the app still works without it.
      });
    }
    const onPrompt = (e: Event) => {
      e.preventDefault(); // keep the event so our own button can trigger it
      setPromptEvent(e as InstallPromptEvent);
    };
    const onInstalled = () => {
      setPromptEvent(null);
      setJustInstalled(true);
    };
    window.addEventListener("beforeinstallprompt", onPrompt);
    window.addEventListener("appinstalled", onInstalled);
    return () => {
      window.removeEventListener("beforeinstallprompt", onPrompt);
      window.removeEventListener("appinstalled", onInstalled);
    };
  }, []);

  async function install() {
    if (!promptEvent) return;
    await promptEvent.prompt();
    await promptEvent.userChoice;
    setPromptEvent(null); // a prompt event can only be used once
  }

  return (
    <PwaContext.Provider
      value={{ canInstall: promptEvent !== null, installed: standalone || justInstalled, isIos, install }}
    >
      {children}
    </PwaContext.Provider>
  );
}
