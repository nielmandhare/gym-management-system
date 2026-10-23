"use client";

import { usePathname } from "next/navigation";
import { AuthProvider } from "@/components/auth/AuthProvider";
import Sidebar from "@/components/layout/Sidebar";
import InstallButton from "@/components/pwa/InstallButton";

const PUBLIC_PAGES = ["/login", "/register"];

/** Login/register get a bare page; everything else gets auth + sidebar. */
export default function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  if (PUBLIC_PAGES.includes(pathname)) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center gap-4 p-4">
        {children}
        <InstallButton className="w-full max-w-sm" />
      </main>
    );
  }
  return (
    <AuthProvider>
      <div className="flex min-h-screen flex-col md:flex-row">
        <Sidebar />
        <main className="flex-1 p-4 md:p-8">{children}</main>
      </div>
    </AuthProvider>
  );
}
