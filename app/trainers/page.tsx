import { RoleGuard } from "@/components/auth/AuthProvider";
import TrainerManager from "@/components/trainers/TrainerManager";

export default function TrainersPage() {
  return (
    <RoleGuard action="trainers:write">
      <TrainerManager />
    </RoleGuard>
  );
}
