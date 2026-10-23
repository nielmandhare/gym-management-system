import { Suspense } from "react";
import { RoleGuard } from "@/components/auth/AuthProvider";
import NewMember from "@/components/members/NewMember";
import { Loading } from "@/components/ui/common";

export default function NewMemberPage() {
  // The form defaults its dates to "today", which is request-time data.
  return (
    <Suspense fallback={<Loading />}>
      <RoleGuard action="members:write">
        <NewMember />
      </RoleGuard>
    </Suspense>
  );
}
