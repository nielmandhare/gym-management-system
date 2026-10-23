import { Suspense } from "react";
import { RoleGuard } from "@/components/auth/AuthProvider";
import MemberDetail from "@/components/members/MemberDetail";
import { Loading } from "@/components/ui/common";

async function Detail({ params }: PageProps<"/members/[id]">) {
  const { id } = await params;
  return (
    <RoleGuard action="members:read">
      <MemberDetail id={id} />
    </RoleGuard>
  );
}

export default function MemberPage(props: PageProps<"/members/[id]">) {
  return (
    <Suspense fallback={<Loading />}>
      <Detail {...props} />
    </Suspense>
  );
}
