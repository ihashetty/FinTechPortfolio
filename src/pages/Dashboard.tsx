import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { portfolioService } from "@/services/portfolioService";
import type { DashboardSummary, PortfolioAllocation, PortfolioGrowth } from "@/types";
import { TrendingUp, TrendingDown, IndianRupee, Percent, ArrowUpRight, ArrowDownRight, Award } from "lucide-react";
import { PieChart, Pie, Cell, ResponsiveContainer, LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid } from "recharts";

const formatCurrency = (val: number) =>
  "₹" + val.toLocaleString("en-IN");

const Dashboard = () => {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [allocation, setAllocation] = useState<PortfolioAllocation[]>([]);
  const [growth, setGrowth] = useState<PortfolioGrowth[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      portfolioService.getDashboard(),
      portfolioService.getAllocation(),
      portfolioService.getGrowth(),
    ]).then(([s, a, g]) => {
      setSummary(s);
      setAllocation(a);
      setGrowth(g);
      setLoading(false);
    });
  }, []);

  if (loading || !summary) {
    return (
      <div className="space-y-6">
        <h1 className="text-2xl font-bold font-display">Dashboard</h1>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {[1, 2, 3, 4].map((i) => (
            <Skeleton key={i} className="h-28 rounded-lg" />
          ))}
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <Skeleton className="h-72 rounded-lg" />
          <Skeleton className="h-72 rounded-lg" />
        </div>
      </div>
    );
  }

  const isProfit = summary.totalPL >= 0;

  const summaryCards = [
    { label: "Total Invested", value: formatCurrency(summary.totalInvested), icon: IndianRupee, color: "text-primary" },
    { label: "Current Value", value: formatCurrency(summary.currentValue), icon: TrendingUp, color: "text-primary" },
    {
      label: "Profit / Loss",
      value: (isProfit ? "+" : "") + formatCurrency(summary.totalPL),
      icon: isProfit ? ArrowUpRight : ArrowDownRight,
      color: isProfit ? "text-profit" : "text-loss",
    },
    { label: "Return %", value: summary.returnPercent.toFixed(2) + "%", icon: Percent, color: isProfit ? "text-profit" : "text-loss" },
  ];

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold font-display">Dashboard</h1>
        <Badge variant="outline" className="text-xs gap-1">
          <Award className="h-3 w-3" /> XIRR: {summary.xirr}%
        </Badge>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {summaryCards.map((c) => (
          <Card key={c.label}>
            <CardContent className="flex items-center gap-4 p-5">
              <div className={`flex h-10 w-10 items-center justify-center rounded-lg bg-secondary ${c.color}`}>
                <c.icon className="h-5 w-5" />
              </div>
              <div>
                <p className="text-xs text-muted-foreground">{c.label}</p>
                <p className={`text-lg font-bold ${c.color}`}>{c.value}</p>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Top Movers */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card>
          <CardContent className="flex items-center gap-3 p-4">
            <TrendingUp className="h-5 w-5 text-profit" />
            <div>
              <p className="text-xs text-muted-foreground">Top Gainer</p>
              <p className="font-semibold text-profit">{summary.topGainer}</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-3 p-4">
            <TrendingDown className="h-5 w-5 text-loss" />
            <div>
              <p className="text-xs text-muted-foreground">Top Loser</p>
              <p className="font-semibold text-loss">{summary.topLoser}</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-3 p-4">
            <Award className="h-5 w-5 text-primary" />
            <div>
              <p className="text-xs text-muted-foreground">Largest Holding</p>
              <p className="font-semibold text-primary">{summary.largestHolding}</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <Card>
          <CardHeader>
            <CardTitle className="text-sm font-medium">Portfolio Allocation</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={250}>
              <PieChart>
                <Pie data={allocation} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={90} innerRadius={50} paddingAngle={3}>
                  {allocation.map((entry, i) => (
                    <Cell key={i} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip formatter={(v: number) => formatCurrency(v)} />
              </PieChart>
            </ResponsiveContainer>
            <div className="flex flex-wrap gap-3 mt-2 justify-center">
              {allocation.map((a) => (
                <div key={a.name} className="flex items-center gap-1 text-xs">
                  <div className="h-2 w-2 rounded-full" style={{ backgroundColor: a.color }} />
                  {a.name}
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-sm font-medium">Portfolio Growth</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={270}>
              <LineChart data={growth}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                <XAxis dataKey="date" tick={{ fontSize: 11 }} className="text-muted-foreground" />
                <YAxis tick={{ fontSize: 11 }} tickFormatter={(v) => "₹" + (v / 1000).toFixed(0) + "K"} className="text-muted-foreground" />
                <Tooltip formatter={(v: number) => formatCurrency(v)} />
                <Line type="monotone" dataKey="value" stroke="hsl(175, 77%, 32%)" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Dashboard;
