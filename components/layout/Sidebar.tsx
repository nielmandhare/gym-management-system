"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/components/auth/AuthProvider";
import InstallButton from "@/components/pwa/InstallButton";
import { useToast } from "@/components/ui/Toast";
import { navLinks } from "@/lib/auth/permissions";
import { api } from "@/lib/client-api";

export default function Sidebar() {
  const pathname = usePathname();
  const router = useRouter();
  const toast = useToast();
  const user = useAuth();
  const links = navLinks(user.role);
  const isActive = (href: string) =>
    href === "/" ? pathname === "/" : pathname.startsWith(href);

  async function logout() {
    try {
      await api("/api/auth/logout", { method: "POST" });
    } finally {
      toast.success("Logged out");
      router.replace("/login");
    }
  }

  return (
    <nav className="border-b border-slate-200 bg-white md:flex md:w-56 md:shrink-0 md:flex-col md:border-b-0 md:border-r">
      <div className="px-4 py-4">
        <div className="text-lg font-semibold text-emerald-700">GymManager</div>
        <div className="mt-1 text-xs text-slate-600">
          <span className="block truncate">{user.full_name || user.email}</span>
          <span className="inline-block rounded bg-emerald-50 px-1.5 py-0.5 font-medium capitalize text-emerald-800">
            {user.role}
          </span>
        </div>
      </div>
      <ul className="flex gap-1 overflow-x-auto px-2 pb-2 md:flex-1 md:flex-col md:pb-0">
        {links.map((l) => (
          <li key={l.href}>
            <Link
              href={l.href}
              aria-current={isActive(l.href) ? "page" : undefined}
              className={`block whitespace-nowrap rounded-md px-3 py-2 text-sm font-medium ${
                isActive(l.href)
                  ? "bg-emerald-50 text-emerald-800"
                  : "text-slate-700 hover:bg-slate-100"
              }`}
            >
              {l.label}
            </Link>
          </li>
        ))}
      </ul>
      <div className="space-y-1 px-2 pb-3 md:pb-4">
        <InstallButton />
        <button
          onClick={logout}
          className="block w-full rounded-md px-3 py-2 text-left text-sm font-medium text-slate-700 hover:bg-slate-100"
        >
          Log out
        </button>
      </div>
    </nav>
  );
}
