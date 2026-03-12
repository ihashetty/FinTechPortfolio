import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { portfolioService } from "@/services/portfolioService";
import { queryKeys } from "@/lib/queryKeys";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { PriceBadge } from "@/components/PriceBadge";
import { TableSkeleton } from "@/components/LoadingSkeleton";
import { Search, Package } from "lucide-react";
import type { AssetType } from "@/types";

const fmt = (v: number) => "₹" + Number(v).toLocaleString("en-IN", { maximumFractionDigits: 2 });

const Holdings = () => {
  const [filter, setFilter] = useState("");
  const [typeFilter, setTypeFilter] = useState<"ALL" | AssetType>("ALL");

  const { data: holdings = [], isLoading } = useQuery({
    queryKey: queryKeys.holdings,
    queryFn: portfolioService.getHoldings,
  });

  const filtered = holdings.filter((h) => {
    const matchText =
      !filter ||
      h.symbol.toLowerCase().includes(filter.toLowerCase()) ||
      h.name.toLowerCase().includes(filter.toLowerCase());
    const matchType = typeFilter === "ALL" || h.assetType === typeFilter;
    return matchText && matchType;
  });

  const totalInvested = filtered.reduce((s, h) => s + Number(h.investedAmount), 0);
  const totalCurrent = filtered.reduce((s, h) => s + Number(h.totalValue), 0);
  const totalPnl = totalCurrent - totalInvested;

  if (isLoading) {
    return (
      <div className="space-y-4">
        <h1 className="text-2xl font-bold font-display">Holdings</h1>
        <TableSkeleton />
      </div>
    );
  }

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold font-display">Holdings</h1>
        <div className="flex items-center gap-3 text-sm">
          <span className="text-muted-foreground">Invested: <strong>{fmt(totalInvested)}</strong></span>
          <span className="text-muted-foreground">Current: <strong>{fmt(totalCurrent)}</strong></span>
          <PriceBadge value={totalPnl} showIcon={false} suffix="" />
        </div>
      </div>

      <Card>
        <CardHeader className="pb-3">
          <div className="flex flex-col sm:flex-row items-start sm:items-center gap-3">
            <div className="flex items-center gap-2 flex-1">
              <Search className="h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Filter by symbol or name..."
                value={filter}
                onChange={(e) => setFilter(e.target.value)}
                className="max-w-xs h-8"
              />
            </div>
            <Select value={typeFilter} onValueChange={(v) => setTypeFilter(v as "ALL" | AssetType)}>
              <SelectTrigger className="w-36 h-8">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">All Types</SelectItem>
                <SelectItem value="STOCK">Stocks</SelectItem>
                <SelectItem value="MF">Mutual Funds</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Asset</TableHead>
                <TableHead>Type</TableHead>
                <TableHead className="text-right">Qty</TableHead>
                <TableHead className="text-right">Avg Price</TableHead>
                <TableHead className="text-right">Current Price</TableHead>
                <TableHead className="text-right">Invested</TableHead>
                <TableHead className="text-right">Current Value</TableHead>
                <TableHead className="text-right">P&L</TableHead>
                <TableHead className="text-right">Return</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filtered.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={9} className="text-center py-12 text-muted-foreground">
                    <Package className="h-8 w-8 mx-auto mb-2 opacity-40" />
                    No holdings found
                  </TableCell>
                </TableRow>
              ) : (
                filtered.map((h) => {
                  const isProfit = Number(h.gainLoss) >= 0;
                  return (
                    <TableRow key={`${h.assetType}-${h.symbol}`}>
                      <TableCell>
                        <span className="font-medium text-sm">{h.symbol}</span>
                        <p className="text-xs text-muted-foreground truncate max-w-[180px]">{h.name}</p>
                      </TableCell>
                      <TableCell>
                        <Badge variant={h.assetType === "MF" ? "secondary" : "outline"} className="text-[10px]">
                          {h.assetType || "STOCK"}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-right text-sm tabular-nums">
                        {h.assetType === "MF"
                          ? Number(h.quantity).toFixed(4)
                          : h.quantity}
                      </TableCell>
                      <TableCell className="text-right text-sm tabular-nums">{fmt(h.avgBuyPrice)}</TableCell>
                      <TableCell className="text-right text-sm tabular-nums">{fmt(h.currentPrice)}</TableCell>
                      <TableCell className="text-right text-sm tabular-nums">{fmt(h.investedAmount)}</TableCell>
                      <TableCell className="text-right text-sm font-medium tabular-nums">{fmt(h.totalValue)}</TableCell>
                      <TableCell className={`text-right text-sm font-medium tabular-nums ${isProfit ? "text-profit" : "text-loss"}`}>
                        {isProfit ? "+" : ""}{fmt(h.gainLoss)}
                      </TableCell>
                      <TableCell className="text-right">
                        <PriceBadge value={Number(h.returnPercent)} suffix="%" showIcon={false} />
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
};

export default Holdings;
