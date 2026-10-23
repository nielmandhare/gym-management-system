import { MEMBERSHIP_PLANS, PLAN_DETAILS } from "@/lib/business/membership";
import { PageHeader } from "@/components/ui/common";

export default function PlansPage() {
  return (
    <div>
      <PageHeader title="Membership Plans" subtitle="Example prices in INR (demonstration values only)" />
      <div className="grid gap-4 md:grid-cols-3">
        {MEMBERSHIP_PLANS.map((plan) => {
          const d = PLAN_DETAILS[plan];
          return (
            <div key={plan} className="rounded-lg border border-slate-200 bg-white p-6">
              <h2 className="text-lg font-semibold text-emerald-700">{plan}</h2>
              <p className="mt-2 text-3xl font-semibold">₹{d.priceInr.toLocaleString("en-IN")}</p>
              <p className="mt-1 text-sm text-slate-600">
                {d.months} {d.months === 1 ? "month" : "months"}
              </p>
              <p className="mt-3 text-sm text-slate-700">{d.summary}</p>
            </div>
          );
        })}
      </div>
    </div>
  );
}
