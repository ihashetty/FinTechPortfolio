import { useEffect, useState } from "react";
import { portfolioService } from "@/services/portfolioService";
import type { Holding } from "@/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";

const Holdings = () => {
  const [holdings, setHoldings] = useState<Holding[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    portfolioService.getHoldings().then((h) => { setHoldings(h); setLoading(false); });
  }, []);

  if (loading) {
    return <div className="space-y-4"><h1 className="text-2xl font-bold font-display">Holdings</h1><Skeleton className="h-96 rounded-lg" /></div>;
  }

  return (
    <div className="space-y-4 animate-fade-in">
      <h1 className="text-2xl font-bold font-display">Holdings</h1>
      <Card>
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Stock</TableHead>
                <TableHead>Sector</TableHead>
                <TableHead className="text-right">Qty</TableHead>
                <TableHead className="text-right">Avg Price</TableHead>
                <TableHead className="text-right">CMP</TableHead>
                <TableHead className="text-right">Value</TableHead>
                <TableHead className="text-right">P&L</TableHead>
                <TableHead className="text-right">Return</TableHead>
                <TableHead className="text-right">Weight</TableHead>
                <TableHead className="text-right">Days</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {holdings.map((h) => {
                const isProfit = h.gainLoss >= 0;
                return (
                  <TableRow key={h.stockSymbol}>
                    <TableCell>
                      <span className="font-medium text-sm">{h.stockSymbol}</span>
                      <p className="text-xs text-muted-foreground">{h.stockName}</p>
                    </TableCell>
                    <TableCell><Badge variant="secondary" className="text-xs">{h.sector}</Badge></TableCell>
                    <TableCell className="text-right text-sm">{h.quantity}</TableCell>
                    <TableCell className="text-right text-sm">₹{h.avgBuyPrice.toLocaleString("en-IN")}</TableCell>
                    <TableCell className="text-right text-sm">₹{h.currentPrice.toLocaleString("en-IN")}</TableCell>
                    <TableCell className="text-right text-sm font-medium">₹{h.totalValue.toLocaleString("en-IN")}</TableCell>
                    <TableCell className={`text-right text-sm font-medium ${isProfit ? "text-profit" : "text-loss"}`}>
                      {isProfit ? "+" : ""}₹{h.gainLoss.toLocaleString("en-IN")}
                    </TableCell>
                    <TableCell className={`text-right text-sm font-medium ${isProfit ? "text-profit" : "text-loss"}`}>
                      {isProfit ? "+" : ""}{h.returnPercent.toFixed(2)}%
                    </TableCell>
                    <TableCell className="text-right text-sm">{h.weightPercent.toFixed(1)}%</TableCell>
                    <TableCell className="text-right text-sm text-muted-foreground">{h.daysHeld}</TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
};

export default Holdings;
