import { RoleGuard } from "@/components/auth/AuthProvider";
import MyProfile from "@/components/users/MyProfile";

export default function MePage() {
  return (
    <RoleGuard action="me:read">
      <MyProfile />
    </RoleGuard>
  );
}
