import { useQuery } from "@tanstack/react-query";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { portfolioService } from "@/services/portfolioService";
import { walletService } from "@/services/walletService";
import { queryKeys } from "@/lib/queryKeys";
import type { Holding } from "@/types";
import { TrendingUp, TrendingDown, IndianRupee, Percent, ArrowUpRight, ArrowDownRight, Award, Wallet, BarChart3, Landmark } from "lucide-react";
import { PieChart, Pie, Cell, ResponsiveContainer, LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, Legend } from "recharts";

const formatCurrency = (val: number) => "₹" + val.toLocaleString("en-IN", { maximumFractionDigits: 0 });

const getHoldingLabel = (h: unknown): string => {
  if (!h) return "—";
  if (typeof h === "string") return h;
  const obj = h as Record<string, unknown>;
  return String(obj.symbol ?? obj.name ?? "—");
};

const ALLOCATION_COLORS = ["#6366f1", "#22c55e", "#f59e0b", "#3b82f6", "#a855f7"];

const Dashboard = () => {
  const { data: summary, isLoading: loadingSummary } = useQuery({
    queryKey: queryKeys.dashboard,
    queryFn: portfolioService.getDashboard,
  });

  const { data: allocation = [], isLoading: loadingAllocation } = useQuery({
    queryKey: queryKeys.allocation,
    queryFn: portfolioService.getAllocation,
  });

  const { data: growth = [], isLoading: loadingGrowth } = useQuery({
    queryKey: queryKeys.growth,
    queryFn: portfolioService.getGrowth,
  });

  const { data: holdings = [] } = useQuery({
    queryKey: queryKeys.holdings,
    queryFn: portfolioService.getHoldings,
  });

  const { data: walletBalance } = useQuery({
    queryKey: queryKeys.walletBalance,
    queryFn: walletService.getBalance,
  });

  const loading = loadingSummary || loadingAllocation || loadingGrowth;

  // Compute stock vs MF split
  const stocksValue = holdings
    .filter((h: Holding) => !h.assetType || h.assetType === "STOCK")
    .reduce((s: number, h: Holding) => s + Number(h.totalValue), 0);
  const mfValue = holdings
    .filter((h: Holding) => h.assetType === "MF")
    .reduce((s: number, h: Holding) => s + Number(h.totalValue), 0);
  const cashBalance = walletBalance ?? 0;
  const totalPortfolioValue = stocksValue + mfValue + cashBalance;

  // Build asset-type allocation for pie chart
  const assetAllocation = [
    { name: "Stocks", value: stocksValue, color: ALLOCATION_COLORS[0] },
    { name: "Mutual Funds", value: mfValue, color: ALLOCATION_COLORS[1] },
    { name: "Cash", value: cashBalance, color: ALLOCATION_COLORS[2] },
  ].filter((a) => a.value > 0);

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
    { label: "Total Portfolio", value: formatCurrency(totalPortfolioValue), icon: IndianRupee, color: "text-primary", sub: "" },
    { label: "Stocks Value", value: formatCurrency(stocksValue), icon: BarChart3, color: "text-indigo-500", sub: totalPortfolioValue > 0 ? `${((stocksValue / totalPortfolioValue) * 100).toFixed(1)}%` : "" },
    { label: "Mutual Funds", value: formatCurrency(mfValue), icon: Landmark, color: "text-emerald-500", sub: totalPortfolioValue > 0 ? `${((mfValue / totalPortfolioValue) * 100).toFixed(1)}%` : "" },
    { label: "Cash Balance", value: formatCurrency(cashBalance), icon: Wallet, color: "text-amber-500", sub: totalPortfolioValue > 0 ? `${((cashBalance / totalPortfolioValue) * 100).toFixed(1)}%` : "" },
  ];

  const plCards = [
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

      {/* Portfolio Composition Cards */}
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
                {c.sub && <p className="text-[10px] text-muted-foreground">{c.sub} of portfolio</p>}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* P&L Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {plCards.map((c) => (
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
              <p className="font-semibold text-profit">{getHoldingLabel(summary.topGainer)}</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-3 p-4">
            <TrendingDown className="h-5 w-5 text-loss" />
            <div>
              <p className="text-xs text-muted-foreground">Top Loser</p>
              <p className="font-semibold text-loss">{getHoldingLabel(summary.topLoser)}</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-3 p-4">
            <Award className="h-5 w-5 text-primary" />
            <div>
              <p className="text-xs text-muted-foreground">Largest Holding</p>
              <p className="font-semibold text-primary">{getHoldingLabel(summary.largestHolding)}</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {/* Asset Allocation Pie */}
        <Card>
          <CardHeader>
            <CardTitle className="text-sm font-medium">Asset Allocation</CardTitle>
          </CardHeader>
          <CardContent>
            {assetAllocation.length === 0 ? (
              <p className="text-center text-muted-foreground py-12">No assets yet</p>
            ) : (
              <>
                <ResponsiveContainer width="100%" height={250}>
                  <PieChart>
                    <Pie
                      data={assetAllocation}
                      dataKey="value"
                      nameKey="name"
                      cx="50%"
                      cy="50%"
                      outerRadius={90}
                      innerRadius={50}
                      paddingAngle={3}
                    >
                      {assetAllocation.map((entry, i) => (
                        <Cell key={i} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(v: number) => formatCurrency(v)} />
                  </PieChart>
                </ResponsiveContainer>
                <div className="flex flex-wrap gap-3 mt-2 justify-center">
                  {assetAllocation.map((a) => (
                    <div key={a.name} className="flex items-center gap-1 text-xs">
                      <div className="h-2 w-2 rounded-full" style={{ backgroundColor: a.color }} />
                      {a.name}: {formatCurrency(a.value)}
                      {totalPortfolioValue > 0 && (
                        <span className="text-muted-foreground">
                          ({((a.value / totalPortfolioValue) * 100).toFixed(1)}%)
                        </span>
                      )}
                    </div>
                  ))}
                </div>
              </>
            )}
          </CardContent>
        </Card>

        {/* Portfolio Growth Line */}
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
                <Legend />
                <Line type="monotone" dataKey="totalValue" name="Value" stroke="hsl(175, 77%, 32%)" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="totalInvested" name="Invested" stroke="hsl(240, 5%, 64%)" strokeWidth={1.5} dot={false} strokeDasharray="5 5" />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Sector Allocation Pie (existing) */}
        {allocation.length > 0 && (
          <Card>
            <CardHeader>
              <CardTitle className="text-sm font-medium">Sector Allocation</CardTitle>
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
        )}
      </div>
    </div>
  );
};

export default Dashboard;
