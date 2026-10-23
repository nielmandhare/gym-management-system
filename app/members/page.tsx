import { RoleGuard } from "@/components/auth/AuthProvider";
import MemberList from "@/components/members/MemberList";

export default function MembersPage() {
  return (
    <RoleGuard action="members:read">
      <MemberList />
    </RoleGuard>
  );
}
