import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { mutualFundService } from "@/services/mutualFundService";
import { walletService } from "@/services/walletService";
import { queryKeys } from "@/lib/queryKeys";
import type { MutualFund, Holding } from "@/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ConfirmTradeModal } from "@/components/ConfirmTradeModal";
import { PriceBadge } from "@/components/PriceBadge";
import { CardSkeleton, TableSkeleton } from "@/components/LoadingSkeleton";
import { useToast } from "@/hooks/use-toast";
import { Search, ShoppingCart, MinusCircle, IndianRupee, TrendingUp } from "lucide-react";

const fmt = (v: number) =>
  "₹" + Number(v).toLocaleString("en-IN", { maximumFractionDigits: 2 });

const MutualFunds = () => {
  const { toast } = useToast();
  const qc = useQueryClient();
  const [filter, setFilter] = useState("");
  const [buyFund, setBuyFund] = useState<MutualFund | null>(null);
  const [sellHolding, setSellHolding] = useState<Holding | null>(null);
  const [buyAmount, setBuyAmount] = useState("");
  const [sellUnits, setSellUnits] = useState("");
  const [confirmBuy, setConfirmBuy] = useState(false);
  const [confirmSell, setConfirmSell] = useState(false);

  const { data: funds = [], isLoading: loadingFunds } = useQuery({
    queryKey: queryKeys.mutualFunds,
    queryFn: mutualFundService.getAll,
  });

  const { data: mfHoldings = [], isLoading: loadingHoldings } = useQuery({
    queryKey: queryKeys.mutualFundHoldings,
    queryFn: mutualFundService.getHoldings,
  });

  const { data: walletBalance } = useQuery({
    queryKey: queryKeys.walletBalance,
    queryFn: walletService.getBalance,
  });

  const invalidateAll = () => {
    qc.invalidateQueries({ queryKey: queryKeys.mutualFunds });
    qc.invalidateQueries({ queryKey: queryKeys.mutualFundHoldings });
    qc.invalidateQueries({ queryKey: queryKeys.holdings });
    qc.invalidateQueries({ queryKey: queryKeys.walletBalance });
    qc.invalidateQueries({ queryKey: queryKeys.walletLedger });
    qc.invalidateQueries({ queryKey: queryKeys.dashboard });
    qc.invalidateQueries({ queryKey: queryKeys.investmentSplit });
  };

  const buyMutation = useMutation({
    mutationFn: mutualFundService.buy,
    onSuccess: () => {
      toast({ title: "Purchase Successful", description: `Invested ₹${buyAmount} in ${buyFund?.symbol}` });
      invalidateAll();
      setBuyFund(null);
      setBuyAmount("");
      setConfirmBuy(false);
    },
    onError: (err: any) => {
      toast({
        title: "Purchase Failed",
        description: err?.response?.data?.message || err?.response?.data || "An error occurred",
        variant: "destructive",
      });
      setConfirmBuy(false);
    },
  });

  const sellMutation = useMutation({
    mutationFn: mutualFundService.sell,
    onSuccess: () => {
      toast({ title: "Redemption Successful", description: `Sold ${sellUnits} units of ${sellHolding?.symbol}` });
      invalidateAll();
      setSellHolding(null);
      setSellUnits("");
      setConfirmSell(false);
    },
    onError: (err: any) => {
      toast({
        title: "Redemption Failed",
        description: err?.response?.data?.message || err?.response?.data || "An error occurred",
        variant: "destructive",
      });
      setConfirmSell(false);
    },
  });

  const filteredFunds = funds.filter(
    (f) =>
      !filter ||
      f.symbol.toLowerCase().includes(filter.toLowerCase()) ||
      f.name.toLowerCase().includes(filter.toLowerCase()) ||
      f.category.toLowerCase().includes(filter.toLowerCase()),
  );

  const buyAmountNum = parseFloat(buyAmount) || 0;
  const estimatedUnits = buyFund ? buyAmountNum / Number(buyFund.nav) : 0;

  const sellUnitsNum = parseFloat(sellUnits) || 0;
  const sellValue = sellHolding ? sellUnitsNum * Number(sellHolding.currentPrice) : 0;

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold font-display">Mutual Funds</h1>
        <span className="text-xs text-muted-foreground">
          Wallet: <strong>{fmt(walletBalance ?? 0)}</strong>
        </span>
      </div>

      <Tabs defaultValue="explore">
        <TabsList>
          <TabsTrigger value="explore">Explore Funds</TabsTrigger>
          <TabsTrigger value="holdings">My MF Holdings ({mfHoldings.length})</TabsTrigger>
        </TabsList>

        {/* EXPLORE TAB */}
        <TabsContent value="explore" className="space-y-4">
          <div className="flex items-center gap-2">
            <Search className="h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search by name, symbol, or category..."
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
              className="max-w-sm h-8"
            />
          </div>

          {loadingFunds ? (
            <CardSkeleton count={6} />
          ) : filteredFunds.length === 0 ? (
            <p className="text-center text-muted-foreground py-12">No mutual funds found</p>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {filteredFunds.map((fund) => (
                <Card key={fund.id} className="hover:shadow-md transition-shadow">
                  <CardContent className="p-4 space-y-3">
                    <div className="flex items-start justify-between">
                      <div className="min-w-0 flex-1">
                        <div className="flex items-center gap-2">
                          <span className="font-semibold text-sm">{fund.symbol}</span>
                          <Badge variant="secondary" className="text-[10px]">{fund.category}</Badge>
                        </div>
                        <p className="text-xs text-muted-foreground truncate mt-0.5">{fund.name}</p>
                      </div>
                      <TrendingUp className="h-4 w-4 text-muted-foreground shrink-0" />
                    </div>
                    <div className="flex items-end justify-between">
                      <div>
                        <p className="text-xs text-muted-foreground">NAV</p>
                        <p className="text-lg font-bold">{fmt(fund.nav)}</p>
                      </div>
                      <Button size="sm" onClick={() => { setBuyFund(fund); setBuyAmount(""); }}>
                        <ShoppingCart className="h-3.5 w-3.5 mr-1" /> Invest
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>

        {/* HOLDINGS TAB */}
        <TabsContent value="holdings" className="space-y-4">
          {loadingHoldings ? (
            <TableSkeleton />
          ) : mfHoldings.length === 0 ? (
            <Card>
              <CardContent className="py-12 text-center text-muted-foreground">
                No mutual fund holdings yet. Start investing!
              </CardContent>
            </Card>
          ) : (
            <Card>
              <CardContent className="p-0">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Fund</TableHead>
                      <TableHead className="text-right">Units</TableHead>
                      <TableHead className="text-right">Avg NAV</TableHead>
                      <TableHead className="text-right">Current NAV</TableHead>
                      <TableHead className="text-right">Invested</TableHead>
                      <TableHead className="text-right">Current</TableHead>
                      <TableHead className="text-right">P&L</TableHead>
                      <TableHead className="text-right">Action</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {mfHoldings.map((h) => (
                      <TableRow key={h.symbol}>
                        <TableCell>
                          <span className="font-medium text-sm">{h.symbol}</span>
                          <p className="text-xs text-muted-foreground truncate max-w-[160px]">{h.name}</p>
                        </TableCell>
                        <TableCell className="text-right text-sm tabular-nums">
                          {Number(h.quantity).toFixed(4)}
                        </TableCell>
                        <TableCell className="text-right text-sm tabular-nums">{fmt(h.avgBuyPrice)}</TableCell>
                        <TableCell className="text-right text-sm tabular-nums">{fmt(h.currentPrice)}</TableCell>
                        <TableCell className="text-right text-sm tabular-nums">{fmt(h.investedAmount)}</TableCell>
                        <TableCell className="text-right text-sm font-medium tabular-nums">{fmt(h.totalValue)}</TableCell>
                        <TableCell className="text-right">
                          <PriceBadge value={Number(h.gainLoss)} showIcon={false} />
                        </TableCell>
                        <TableCell className="text-right">
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => { setSellHolding(h); setSellUnits(""); }}
                          >
                            <MinusCircle className="h-3.5 w-3.5 mr-1" /> Redeem
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          )}
        </TabsContent>
      </Tabs>

      {/* BUY DIALOG */}
      <Dialog open={!!buyFund} onOpenChange={() => setBuyFund(null)}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <ShoppingCart className="h-5 w-5 text-primary" />
              Invest in {buyFund?.symbol}
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="text-sm text-muted-foreground">
              NAV: <strong>{fmt(buyFund?.nav ?? 0)}</strong> · Wallet: <strong>{fmt(walletBalance ?? 0)}</strong>
            </div>
            <div className="space-y-1">
              <Label className="text-xs">Investment Amount (₹)</Label>
              <div className="relative">
                <IndianRupee className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  type="number"
                  min={100}
                  step={100}
                  placeholder="Min ₹100"
                  value={buyAmount}
                  onChange={(e) => setBuyAmount(e.target.value)}
                  className="pl-9"
                />
              </div>
            </div>
            {buyAmountNum > 0 && (
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Estimated units</span>
                <span className="font-medium">{estimatedUnits.toFixed(4)}</span>
              </div>
            )}
            {buyAmountNum > (walletBalance ?? 0) && (
              <p className="text-xs text-loss">Insufficient wallet balance</p>
            )}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setBuyFund(null)}>Cancel</Button>
            <Button
              disabled={buyAmountNum < 100 || buyAmountNum > (walletBalance ?? 0)}
              onClick={() => setConfirmBuy(true)}
            >
              Review Order
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <ConfirmTradeModal
        open={confirmBuy}
        onOpenChange={setConfirmBuy}
        title="Confirm Investment"
        description={`You are investing in ${buyFund?.name}`}
        lines={[
          { label: "Fund", value: buyFund?.symbol ?? "" },
          { label: "NAV", value: fmt(buyFund?.nav ?? 0) },
          { label: "Amount", value: fmt(buyAmountNum) },
          { label: "Est. Units", value: estimatedUnits.toFixed(4) },
          { label: "Total Debit", value: fmt(buyAmountNum), bold: true },
        ]}
        confirmLabel="Confirm Investment"
        loading={buyMutation.isPending}
        onConfirm={() => buyFund && buyMutation.mutate({ symbol: buyFund.symbol, amount: buyAmountNum })}
      />

      {/* SELL DIALOG */}
      <Dialog open={!!sellHolding} onOpenChange={() => setSellHolding(null)}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <MinusCircle className="h-5 w-5 text-loss" />
              Redeem {sellHolding?.symbol}
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="text-sm text-muted-foreground">
              Available Units: <strong>{Number(sellHolding?.quantity ?? 0).toFixed(4)}</strong> ·
              NAV: <strong>{fmt(sellHolding?.currentPrice ?? 0)}</strong>
            </div>
            <div className="space-y-1">
              <Label className="text-xs">Units to Redeem</Label>
              <Input
                type="number"
                min={0.0001}
                step={0.0001}
                placeholder="0.0000"
                value={sellUnits}
                onChange={(e) => setSellUnits(e.target.value)}
              />
            </div>
            <div className="flex justify-between">
              <Button
                size="sm"
                variant="ghost"
                className="text-xs"
                onClick={() => setSellUnits(String(sellHolding?.quantity ?? 0))}
              >
                Redeem All
              </Button>
              {sellUnitsNum > 0 && (
                <span className="text-sm text-muted-foreground">
                  ≈ {fmt(sellValue)}
                </span>
              )}
            </div>
            {sellUnitsNum > Number(sellHolding?.quantity ?? 0) && (
              <p className="text-xs text-loss">Exceeds available units</p>
            )}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setSellHolding(null)}>Cancel</Button>
            <Button
              disabled={sellUnitsNum <= 0 || sellUnitsNum > Number(sellHolding?.quantity ?? 0)}
              onClick={() => setConfirmSell(true)}
            >
              Review Order
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <ConfirmTradeModal
        open={confirmSell}
        onOpenChange={setConfirmSell}
        title="Confirm Redemption"
        description={`You are redeeming units of ${sellHolding?.name}`}
        lines={[
          { label: "Fund", value: sellHolding?.symbol ?? "" },
          { label: "Units", value: sellUnitsNum.toFixed(4) },
          { label: "NAV", value: fmt(sellHolding?.currentPrice ?? 0) },
          { label: "Est. Credit", value: fmt(sellValue), bold: true },
        ]}
        confirmLabel="Confirm Redemption"
        loading={sellMutation.isPending}
        onConfirm={() =>
          sellHolding &&
          sellMutation.mutate({ symbol: sellHolding.symbol, units: sellUnitsNum })
        }
      />
    </div>
  );
};

export default MutualFunds;
