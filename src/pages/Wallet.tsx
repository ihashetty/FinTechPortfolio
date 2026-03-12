import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { walletService } from "@/services/walletService";
import { queryKeys } from "@/lib/queryKeys";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
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
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/hooks/use-toast";
import { CardSkeleton, TableSkeleton } from "@/components/LoadingSkeleton";
import {
  Wallet as WalletIcon,
  ArrowDownToLine,
  ArrowUpFromLine,
  IndianRupee,
} from "lucide-react";

const fmt = (v: number) =>
  "₹" + Number(v).toLocaleString("en-IN", { maximumFractionDigits: 2 });

const Wallet = () => {
  const { toast } = useToast();
  const qc = useQueryClient();
  const [modalType, setModalType] = useState<"deposit" | "withdraw" | null>(null);
  const [amount, setAmount] = useState("");

  const { data: balance, isLoading: loadingBalance } = useQuery({
    queryKey: queryKeys.walletBalance,
    queryFn: walletService.getBalance,
  });

  const { data: ledger = [], isLoading: loadingLedger } = useQuery({
    queryKey: queryKeys.walletLedger,
    queryFn: walletService.getLedger,
  });

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: queryKeys.walletBalance });
    qc.invalidateQueries({ queryKey: queryKeys.walletLedger });
  };

  const depositMutation = useMutation({
    mutationFn: walletService.deposit,
    onSuccess: () => {
      toast({ title: "Deposit Successful", description: `₹${amount} added to wallet` });
      invalidate();
      closeModal();
    },
    onError: () =>
      toast({ title: "Deposit Failed", description: "Please try again", variant: "destructive" }),
  });

  const withdrawMutation = useMutation({
    mutationFn: walletService.withdraw,
    onSuccess: () => {
      toast({ title: "Withdrawal Successful", description: `₹${amount} withdrawn` });
      invalidate();
      closeModal();
    },
    onError: (err: any) => {
      const msg = err?.response?.data?.message || "Insufficient balance or error";
      toast({ title: "Withdrawal Failed", description: msg, variant: "destructive" });
    },
  });

  const closeModal = () => {
    setModalType(null);
    setAmount("");
  };

  const handleSubmit = () => {
    const num = parseFloat(amount);
    if (!num || num <= 0) {
      toast({ title: "Invalid amount", description: "Enter a positive number", variant: "destructive" });
      return;
    }
    if (modalType === "deposit") depositMutation.mutate(num);
    else withdrawMutation.mutate(num);
  };

  const isPending = depositMutation.isPending || withdrawMutation.isPending;

  const ledgerTypeColor = (type: string) => {
    switch (type) {
      case "DEPOSIT":
        return "default";
      case "WITHDRAWAL":
        return "destructive";
      case "BUY":
        return "secondary";
      case "SELL":
        return "outline";
      default:
        return "secondary";
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold font-display">Wallet</h1>
        <div className="flex gap-2">
          <Button size="sm" onClick={() => setModalType("deposit")}>
            <ArrowDownToLine className="h-4 w-4 mr-1" /> Deposit
          </Button>
          <Button size="sm" variant="outline" onClick={() => setModalType("withdraw")}>
            <ArrowUpFromLine className="h-4 w-4 mr-1" /> Withdraw
          </Button>
        </div>
      </div>

      {/* Balance Card */}
      {loadingBalance ? (
        <CardSkeleton count={1} />
      ) : (
        <Card>
          <CardContent className="flex items-center gap-4 p-6">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 text-primary">
              <WalletIcon className="h-6 w-6" />
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Available Balance</p>
              <p className="text-3xl font-bold">{fmt(balance ?? 0)}</p>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Ledger */}
      <Card>
        <CardHeader>
          <CardTitle className="text-sm font-medium">Transaction Ledger</CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          {loadingLedger ? (
            <div className="p-4">
              <TableSkeleton />
            </div>
          ) : ledger.length === 0 ? (
            <p className="text-center text-muted-foreground py-12">No ledger entries yet</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Date</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead className="text-right">Amount</TableHead>
                  <TableHead className="text-right">Ref ID</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {ledger.map((entry) => (
                  <TableRow key={entry.id}>
                    <TableCell className="text-sm">
                      {new Date(entry.createdAt).toLocaleString("en-IN", {
                        dateStyle: "medium",
                        timeStyle: "short",
                      })}
                    </TableCell>
                    <TableCell>
                      <Badge variant={ledgerTypeColor(entry.type) as any} className="text-xs">
                        {entry.type}
                      </Badge>
                    </TableCell>
                    <TableCell
                      className={`text-right text-sm font-medium ${
                        entry.type === "DEPOSIT" || entry.type === "SELL"
                          ? "text-profit"
                          : "text-loss"
                      }`}
                    >
                      {entry.type === "DEPOSIT" || entry.type === "SELL" ? "+" : "-"}
                      {fmt(Math.abs(Number(entry.amount)))}
                    </TableCell>
                    <TableCell className="text-right text-xs text-muted-foreground">
                      {entry.referenceId ?? "—"}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* Deposit / Withdraw Modal */}
      <Dialog open={!!modalType} onOpenChange={() => closeModal()}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              {modalType === "deposit" ? (
                <ArrowDownToLine className="h-5 w-5 text-profit" />
              ) : (
                <ArrowUpFromLine className="h-5 w-5 text-loss" />
              )}
              {modalType === "deposit" ? "Deposit Funds" : "Withdraw Funds"}
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="text-sm text-muted-foreground">
              Current balance: <strong>{fmt(balance ?? 0)}</strong>
            </div>
            <div className="space-y-1">
              <Label className="text-xs">Amount (₹)</Label>
              <div className="relative">
                <IndianRupee className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  type="number"
                  min={1}
                  step={0.01}
                  placeholder="0.00"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="pl-9"
                />
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={closeModal} disabled={isPending}>
              Cancel
            </Button>
            <Button onClick={handleSubmit} disabled={isPending}>
              {isPending
                ? "Processing…"
                : modalType === "deposit"
                  ? "Deposit"
                  : "Withdraw"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default Wallet;
