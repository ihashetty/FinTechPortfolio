import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { transactionService } from "@/services/transactionService";
import { walletService } from "@/services/walletService";
import { queryKeys } from "@/lib/queryKeys";
import type { Transaction, CreateTransactionDTO, Stock } from "@/types";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { ConfirmTradeModal } from "@/components/ConfirmTradeModal";
import { StockPicker } from "@/components/StockPicker";
import { useToast } from "@/hooks/use-toast";
import { Plus, Pencil, Trash2, Search } from "lucide-react";

const BROKERAGE = 20;
const fmt = (v: number) => "₹" + Number(v).toLocaleString("en-IN", { maximumFractionDigits: 2 });

const emptyForm: CreateTransactionDTO = {
  stockSymbol: "",
  stockName: "",
  type: "BUY",
  quantity: 0,
  price: undefined,
  transactionDate: new Date().toISOString().slice(0, 10),
  brokerage: BROKERAGE,
  notes: "",
  orderType: "MARKET",
};

const Transactions = () => {
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const [dialogOpen, setDialogOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Transaction | null>(null);
  const [form, setForm] = useState<CreateTransactionDTO>(emptyForm);
  const [filter, setFilter] = useState("");
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [marketPrice, setMarketPrice] = useState<number | null>(null);

  const { data: walletBalance } = useQuery({
    queryKey: queryKeys.walletBalance,
    queryFn: walletService.getBalance,
  });

  const { data: transactions = [], isLoading } = useQuery({
    queryKey: queryKeys.transactions(),
    queryFn: () => transactionService.getAll(),
  });

  // When user selects a stock from the picker
  const handleStockSelect = (stock: Stock) => {
    setMarketPrice(stock.currentPrice);
    setForm((prev) => ({
      ...prev,
      stockSymbol: stock.symbol,
      stockName: stock.name,
      // For market orders, display market price; for limit, keep user's price
      price: prev.orderType === "MARKET" ? undefined : prev.price,
    }));
  };

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: queryKeys.transactions() });
    queryClient.invalidateQueries({ queryKey: queryKeys.holdings });
    queryClient.invalidateQueries({ queryKey: queryKeys.walletBalance });
    queryClient.invalidateQueries({ queryKey: queryKeys.walletLedger });
    queryClient.invalidateQueries({ queryKey: queryKeys.dashboard });
  };

  const addMutation = useMutation({
    mutationFn: transactionService.add,
    onSuccess: () => { toast({ title: "Added", description: "Transaction added successfully" }); setDialogOpen(false); setConfirmOpen(false); invalidate(); },
    onError: (err: any) => {
      toast({ title: "Error", description: err?.response?.data?.message || "Failed to add transaction", variant: "destructive" });
      setConfirmOpen(false);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: CreateTransactionDTO }) => transactionService.update(id, data),
    onSuccess: () => { toast({ title: "Updated", description: "Transaction updated successfully" }); setDialogOpen(false); invalidate(); },
    onError: () => toast({ title: "Error", description: "Failed to update transaction", variant: "destructive" }),
  });

  const deleteMutation = useMutation({
    mutationFn: transactionService.delete,
    onSuccess: () => { toast({ title: "Deleted", description: "Transaction removed" }); setDeleteId(null); invalidate(); },
    onError: () => toast({ title: "Error", description: "Failed to delete transaction", variant: "destructive" }),
  });

  const openAdd = () => { setEditing(null); setForm(emptyForm); setMarketPrice(null); setDialogOpen(true); };
  const openEdit = (tx: Transaction) => {
    setEditing(tx);
    setForm({ stockSymbol: tx.stockSymbol, stockName: tx.stockName, type: tx.type, quantity: tx.quantity, price: tx.price, transactionDate: tx.transactionDate, brokerage: tx.brokerage, notes: tx.notes ?? "" });
    setDialogOpen(true);
  };

  const effectivePrice = form.orderType === "MARKET" ? (marketPrice ?? 0) : (form.price ?? 0);
  const totalCost = form.quantity * effectivePrice + form.brokerage;
  const insufficientBalance = form.type === "BUY" && totalCost > (walletBalance ?? 0);

  const handleReview = () => {
    if (!form.stockSymbol || form.quantity <= 0 || !form.transactionDate) {
      toast({ title: "Validation Error", description: "Please fill all required fields with valid values", variant: "destructive" });
      return;
    }
    if (form.orderType === "LIMIT" && (!form.price || form.price <= 0)) {
      toast({ title: "Validation Error", description: "Price is required for Limit orders", variant: "destructive" });
      return;
    }
    if (form.orderType === "MARKET" && !marketPrice) {
      toast({ title: "Validation Error", description: "Please select a valid stock to get market price", variant: "destructive" });
      return;
    }
    if (insufficientBalance && !editing) {
      toast({ title: "Insufficient Balance", description: `Need ${fmt(totalCost)} but wallet has only ${fmt(walletBalance ?? 0)}`, variant: "destructive" });
      return;
    }
    if (editing) {
      // Direct update for edits
      updateMutation.mutate({ id: editing.id, data: form });
    } else {
      setConfirmOpen(true);
    }
  };

  const handleConfirmTrade = () => {
    // For MARKET orders, send price as undefined — backend resolves it
    const payload: CreateTransactionDTO = {
      ...form,
      price: form.orderType === "MARKET" ? undefined : form.price,
      orderType: form.orderType ?? "MARKET",
    };
    addMutation.mutate(payload);
  };

  const filtered = transactions.filter(
    (t) => !filter || t.stockSymbol.toLowerCase().includes(filter.toLowerCase()) || t.stockName.toLowerCase().includes(filter.toLowerCase())
  );

  if (isLoading) {
    return (
      <div className="space-y-4">
        <h1 className="text-2xl font-bold font-display">Transactions</h1>
        <Skeleton className="h-96 rounded-lg" />
      </div>
    );
  }

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <h1 className="text-2xl font-bold font-display">Transactions</h1>
        <div className="flex items-center gap-3">
          <span className="text-xs text-muted-foreground">
            Wallet: <strong>{fmt(walletBalance ?? 0)}</strong>
          </span>
          <Button onClick={openAdd} size="sm"><Plus className="h-4 w-4 mr-1" /> Add Transaction</Button>
        </div>
      </div>

      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-center gap-2">
            <Search className="h-4 w-4 text-muted-foreground" />
            <Input placeholder="Filter by stock..." value={filter} onChange={(e) => setFilter(e.target.value)} className="max-w-xs h-8" />
          </div>
        </CardHeader>
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Date</TableHead>
                <TableHead>Stock</TableHead>
                <TableHead>Type</TableHead>
                <TableHead className="text-right">Qty</TableHead>
                <TableHead className="text-right">Price (₹)</TableHead>
                <TableHead className="text-right">Brokerage</TableHead>
                <TableHead className="text-right">Total (₹)</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filtered.length === 0 ? (
                <TableRow><TableCell colSpan={8} className="text-center py-8 text-muted-foreground">No transactions found</TableCell></TableRow>
              ) : (
                filtered.map((tx) => (
                  <TableRow key={tx.id}>
                    <TableCell className="text-sm">{tx.transactionDate}</TableCell>
                    <TableCell>
                      <div>
                        <span className="font-medium text-sm">{tx.stockSymbol}</span>
                        <p className="text-xs text-muted-foreground">{tx.stockName}</p>
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge variant={tx.type === "BUY" ? "default" : "destructive"} className="text-xs">
                        {tx.type}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right text-sm tabular-nums">{tx.quantity}</TableCell>
                    <TableCell className="text-right text-sm tabular-nums">{fmt(tx.price)}</TableCell>
                    <TableCell className="text-right text-sm tabular-nums text-muted-foreground">{fmt(tx.brokerage)}</TableCell>
                    <TableCell className="text-right text-sm font-medium tabular-nums">{fmt(tx.quantity * Number(tx.price) + Number(tx.brokerage))}</TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-1">
                        <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => openEdit(tx)}><Pencil className="h-3 w-3" /></Button>
                        <Button variant="ghost" size="icon" className="h-7 w-7 text-destructive" onClick={() => setDeleteId(tx.id)}><Trash2 className="h-3 w-3" /></Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {/* Add/Edit Dialog */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editing ? "Edit Transaction" : "New Trade"}</DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-2">
            {/* Stock Picker */}
            <div className="space-y-1">
              <Label className="text-xs">Stock</Label>
              {editing ? (
                <div className="flex items-center gap-2 h-9 px-3 border rounded-md bg-muted text-sm">
                  <span className="font-semibold">{form.stockSymbol}</span>
                  <span className="text-muted-foreground">{form.stockName}</span>
                </div>
              ) : (
                <StockPicker
                  value={form.stockSymbol}
                  onSelect={handleStockSelect}
                  placeholder="Search stocks by name or symbol..."
                />
              )}
            </div>

            <div className="grid grid-cols-3 gap-3">
              <div className="space-y-1">
                <Label className="text-xs">Type</Label>
                <Select value={form.type} onValueChange={(v) => setForm({ ...form, type: v as "BUY" | "SELL" })}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="BUY">BUY</SelectItem>
                    <SelectItem value="SELL">SELL</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1">
                <Label className="text-xs">Quantity</Label>
                <Input type="number" min={1} value={form.quantity || ""} onChange={(e) => setForm({ ...form, quantity: Number(e.target.value) })} />
              </div>
              <div className="space-y-1">
                <Label className="text-xs">Order Type</Label>
                <Select
                  value={form.orderType ?? "MARKET"}
                  onValueChange={(v) => {
                    const ot = v as "MARKET" | "LIMIT";
                    setForm((prev) => ({
                      ...prev,
                      orderType: ot,
                      price: ot === "MARKET" ? undefined : prev.price,
                    }));
                  }}
                >
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="MARKET">Market</SelectItem>
                    <SelectItem value="LIMIT">Limit</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            {/* Price: Market vs Limit */}
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                {form.orderType === "LIMIT" ? (
                  <>
                    <Label className="text-xs">Limit Price (₹)</Label>
                    <Input
                      type="number"
                      min={0.01}
                      step={0.01}
                      value={form.price ?? ""}
                      onChange={(e) => setForm({ ...form, price: Number(e.target.value) })}
                      placeholder="Enter your price"
                    />
                  </>
                ) : (
                  <>
                    <Label className="text-xs">Market Price (₹)</Label>
                    <div className="flex items-center h-9 px-3 border rounded-md bg-muted text-sm tabular-nums">
                      {marketPrice ? (
                        <span className="text-profit font-medium">{fmt(marketPrice)}</span>
                      ) : (
                        <span className="text-muted-foreground">Select a stock</span>
                      )}
                    </div>
                    <p className="text-[10px] text-muted-foreground">
                      Will execute at current market price
                    </p>
                  </>
                )}
              </div>
              <div className="space-y-1">
                <Label className="text-xs">Date</Label>
                <Input type="date" value={form.transactionDate} onChange={(e) => setForm({ ...form, transactionDate: e.target.value })} />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label className="text-xs">Brokerage (₹)</Label>
                <Input type="number" min={0} value={form.brokerage || ""} readOnly className="bg-muted" />
              </div>
            </div>
            <div className="space-y-1">
              <Label className="text-xs">Notes</Label>
              <Input value={form.notes ?? ""} onChange={(e) => setForm({ ...form, notes: e.target.value })} placeholder="Optional notes" />
            </div>

            {/* Cost Summary */}
            {form.quantity > 0 && effectivePrice > 0 && (
              <div className="rounded-lg bg-secondary/50 p-3 space-y-1 text-sm">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">
                    {form.quantity} × {fmt(effectivePrice)}
                    {form.orderType === "MARKET" && (
                      <span className="ml-1 text-[10px]">(Market)</span>
                    )}
                  </span>
                  <span>{fmt(form.quantity * effectivePrice)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Brokerage</span>
                  <span>{fmt(form.brokerage)}</span>
                </div>
                <div className="flex justify-between font-semibold border-t pt-1">
                  <span>Total {form.type === "BUY" ? "Cost" : "Credit"}</span>
                  <span>{fmt(totalCost)}</span>
                </div>
                {insufficientBalance && !editing && (
                  <p className="text-xs text-loss mt-1">
                    ⚠ Insufficient balance. Wallet: {fmt(walletBalance ?? 0)}
                  </p>
                )}
              </div>
            )}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>Cancel</Button>
            <Button
              onClick={handleReview}
              disabled={addMutation.isPending || updateMutation.isPending || (insufficientBalance && !editing)}
            >
              {editing ? "Update" : "Review & Place"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Confirm Trade Modal (new trades only) */}
      <ConfirmTradeModal
        open={confirmOpen}
        onOpenChange={setConfirmOpen}
        title={`Confirm ${form.type} Order`}
        description={`${form.type === "BUY" ? "Buying" : "Selling"} ${form.stockSymbol} — ${form.orderType === "MARKET" ? "Market Order" : `Limit @ ${fmt(form.price ?? 0)}`}`}
        lines={[
          { label: "Stock", value: `${form.stockSymbol} — ${form.stockName}` },
          { label: "Type", value: form.type },
          { label: "Order Type", value: form.orderType === "MARKET" ? "Market" : "Limit" },
          { label: "Quantity", value: String(form.quantity) },
          { label: "Price", value: form.orderType === "MARKET" ? `${fmt(marketPrice ?? 0)} (Market)` : fmt(form.price ?? 0) },
          { label: "Brokerage", value: fmt(form.brokerage) },
          { label: `Total ${form.type === "BUY" ? "Debit" : "Credit"}`, value: fmt(totalCost), bold: true },
        ]}
        confirmLabel={`Place ${form.type} Order`}
        loading={addMutation.isPending}
        onConfirm={handleConfirmTrade}
      />

      {/* Delete Confirmation */}
      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Transaction?</AlertDialogTitle>
            <AlertDialogDescription>This action cannot be undone.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => deleteId !== null && deleteMutation.mutate(deleteId)}
              className="bg-destructive text-destructive-foreground"
            >
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

export default Transactions;
