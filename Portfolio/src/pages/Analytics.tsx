import { useQuery } from "@tanstack/react-query";
import { analyticsService } from "@/services/analyticsService";
import { queryKeys } from "@/lib/queryKeys";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, PieChart, Pie, Cell } from "recharts";

const Analytics = () => {
  const { data: monthlyPL = [], isLoading: loadingPL } = useQuery({
    queryKey: queryKeys.monthlyPL,
    queryFn: analyticsService.getMonthlyPL,
  });

  const { data: sectors = [], isLoading: loadingSectors } = useQuery({
    queryKey: queryKeys.sectorAllocation,
    queryFn: analyticsService.getSectorAllocation,
  });

  const { data: taxSummary, isLoading: loadingTax } = useQuery({
    queryKey: queryKeys.taxSummary(),
    queryFn: () => analyticsService.getTaxSummary(),
  });

  const loading = loadingPL || loadingSectors || loadingTax;

  if (loading) {
    return (
      <div className="space-y-4">
        <h1 className="text-2xl font-bold font-display">Analytics</h1>
        <Skeleton className="h-96 rounded-lg" />
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <h1 className="text-2xl font-bold font-display">Analytics</h1>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <Card>
          <CardHeader><CardTitle className="text-sm font-medium">Monthly P&L</CardTitle></CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={monthlyPL}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                <XAxis dataKey="month" tick={{ fontSize: 10 }} />
                <YAxis tick={{ fontSize: 10 }} tickFormatter={(v) => "₹" + (v / 1000).toFixed(0) + "K"} />
                <Tooltip formatter={(v: number) => "₹" + v.toLocaleString("en-IN")} />
                <Bar dataKey="profit" fill="hsl(142, 71%, 45%)" radius={[4, 4, 0, 0]} />
                <Bar dataKey="loss" fill="hsl(0, 84%, 60%)" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle className="text-sm font-medium">Sector Allocation</CardTitle></CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={250}>
              <PieChart>
                <Pie data={sectors} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={90} innerRadius={55} paddingAngle={3}>
                  {sectors.map((s, i) => <Cell key={i} fill={s.color} />)}
                </Pie>
                <Tooltip formatter={(v: number) => v.toFixed(1) + "%"} />
              </PieChart>
            </ResponsiveContainer>
            <div className="flex flex-wrap gap-3 justify-center">
              {sectors.map((s) => (
                <div key={s.name} className="flex items-center gap-1 text-xs">
                  <div className="h-2 w-2 rounded-full" style={{ backgroundColor: s.color }} />
                  {s.name} ({Number(s.value).toFixed(1)}%)
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Tax Summary cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardHeader><CardTitle className="text-sm font-medium">Short Term Gains</CardTitle></CardHeader>
          <CardContent>
            <p className={`text-2xl font-bold ${(taxSummary?.shortTermGains ?? 0) >= 0 ? "text-profit" : "text-loss"}`}>
              {(taxSummary?.shortTermGains ?? 0) >= 0 ? "+" : ""}₹{Number(taxSummary?.shortTermGains ?? 0).toLocaleString("en-IN")}
            </p>
            <p className="text-xs text-muted-foreground mt-1">FY {taxSummary?.fy ?? "—"}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle className="text-sm font-medium">Long Term Gains</CardTitle></CardHeader>
          <CardContent>
            <p className={`text-2xl font-bold ${(taxSummary?.longTermGains ?? 0) >= 0 ? "text-profit" : "text-loss"}`}>
              {(taxSummary?.longTermGains ?? 0) >= 0 ? "+" : ""}₹{Number(taxSummary?.longTermGains ?? 0).toLocaleString("en-IN")}
            </p>
            <p className="text-xs text-muted-foreground mt-1">FY {taxSummary?.fy ?? "—"}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle className="text-sm font-medium">STCG Tax (15%)</CardTitle></CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-foreground">₹{Number(taxSummary?.stcgTax ?? 0).toLocaleString("en-IN")}</p>
            <p className="text-xs text-muted-foreground mt-1">Short-term liability</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle className="text-sm font-medium">LTCG Tax (10%)</CardTitle></CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-foreground">₹{Number(taxSummary?.ltcgTax ?? 0).toLocaleString("en-IN")}</p>
            <p className="text-xs text-muted-foreground mt-1">Long-term liability</p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Analytics;
