import { useEffect, useState } from "react";
import { portfolioService } from "@/services/portfolioService";
import type { MonthlyPL, SectorAllocation } from "@/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, PieChart, Pie, Cell } from "recharts";

const Analytics = () => {
  const [monthlyPL, setMonthlyPL] = useState<MonthlyPL[]>([]);
  const [sectors, setSectors] = useState<SectorAllocation[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([portfolioService.getMonthlyPL(), portfolioService.getSectorAllocation()]).then(([m, s]) => {
      setMonthlyPL(m);
      setSectors(s);
      setLoading(false);
    });
  }, []);

  if (loading) {
    return <div className="space-y-4"><h1 className="text-2xl font-bold font-display">Analytics</h1><Skeleton className="h-96 rounded-lg" /></div>;
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
                <Pie data={sectors} dataKey="value" nameKey="sector" cx="50%" cy="50%" outerRadius={90} innerRadius={55} paddingAngle={3}>
                  {sectors.map((s, i) => <Cell key={i} fill={s.color} />)}
                </Pie>
                <Tooltip formatter={(v: number) => v.toFixed(1) + "%"} />
              </PieChart>
            </ResponsiveContainer>
            <div className="flex flex-wrap gap-3 justify-center">
              {sectors.map((s) => (
                <div key={s.sector} className="flex items-center gap-1 text-xs">
                  <div className="h-2 w-2 rounded-full" style={{ backgroundColor: s.color }} />
                  {s.sector} ({s.value}%)
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Placeholder cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardHeader><CardTitle className="text-sm font-medium">Realized Gains</CardTitle></CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-profit">+₹1,200</p>
            <p className="text-xs text-muted-foreground mt-1">From {1} sold position</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle className="text-sm font-medium">Unrealized Gains</CardTitle></CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-profit">+₹40,780</p>
            <p className="text-xs text-muted-foreground mt-1">Across {9} holdings</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle className="text-sm font-medium">Risk Meter</CardTitle></CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-primary">Medium</p>
            <p className="text-xs text-muted-foreground mt-1">Based on sector diversification</p>
            {/* FUTURE: Backend volatility calculation */}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Analytics;
