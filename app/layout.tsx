import type { Metadata, Viewport } from "next";
import { Suspense } from "react";
import "./globals.css";
import AppShell from "@/components/layout/AppShell";
import PwaProvider from "@/components/pwa/PwaProvider";
import { ToastProvider } from "@/components/ui/Toast";

export const metadata: Metadata = {
  title: "Gym Management System",
  description: "Simple gym administration dashboard (STQA mini project)",
  applicationName: "GymManager",
  appleWebApp: { capable: true, title: "GymManager", statusBarStyle: "default" },
};

export const viewport: Viewport = {
  themeColor: "#059669",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="en" className="h-full antialiased">
      <body className="min-h-full">
        <ToastProvider>
          <PwaProvider>
            <Suspense fallback={null}>
              <AppShell>{children}</AppShell>
            </Suspense>
          </PwaProvider>
        </ToastProvider>
      </body>
    </html>
  );
}
