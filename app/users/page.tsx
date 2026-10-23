import { RoleGuard } from "@/components/auth/AuthProvider";
import UserManager from "@/components/users/UserManager";

export default function UsersPage() {
  return (
    <RoleGuard action="users:manage">
      <UserManager />
    </RoleGuard>
  );
}
