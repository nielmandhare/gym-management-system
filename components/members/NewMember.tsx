"use client";

import { useRouter } from "next/navigation";
import MemberForm from "@/components/members/MemberForm";
import { PageHeader } from "@/components/ui/common";
import { useToast } from "@/components/ui/Toast";
import { api } from "@/lib/client-api";
import type { Member } from "@/lib/services/members";

export default function NewMember() {
  const router = useRouter();
  const toast = useToast();

  return (
    <div className="max-w-3xl">
      <PageHeader title="Add Member" subtitle="Register a new gym member" />
      <MemberForm
        submitLabel="Add member"
        onCancel={() => router.push("/members")}
        onSubmit={async (values) => {
          const m = await api<Member>("/api/members", { method: "POST", body: JSON.stringify(values) });
          toast.success(`Member "${m.full_name}" added`);
          router.push("/members");
        }}
      />
    </div>
  );
}
