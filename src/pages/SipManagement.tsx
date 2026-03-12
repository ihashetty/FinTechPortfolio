import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { sipService } from "@/services/sipService";
import { mutualFundService } from "@/services/mutualFundService";
import { queryKeys } from "@/lib/queryKeys";
import type { MutualFund } from "@/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { CardSkeleton, TableSkeleton } from "@/components/LoadingSkeleton";
import { useToast } from "@/hooks/use-toast";
import { Plus, CalendarClock, Trash2, IndianRupee, RefreshCw } from "lucide-react";

const fmt = (v: number) =>
  "₹" + Number(v).toLocaleString("en-IN", { maximumFractionDigits: 2 });

const SipManagement = () => {
  const { toast } = useToast();
  const qc = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [cancelId, setCancelId] = useState<number | null>(null);
  const [selectedFund, setSelectedFund] = useState("");
  const [sipAmount, setSipAmount] = useState("");
  const [frequency, setFrequency] = useState("MONTHLY");

  const { data: sips = [], isLoading: loadingSips } = useQuery({
    queryKey: queryKeys.sips,
    queryFn: sipService.getAll,
  });

  const { data: funds = [] } = useQuery({
    queryKey: queryKeys.mutualFunds,
    queryFn: mutualFundService.getAll,
  });

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: queryKeys.sips });
    qc.invalidateQueries({ queryKey: queryKeys.dashboard });
    qc.invalidateQueries({ queryKey: queryKeys.holdings });
    qc.invalidateQueries({ queryKey: queryKeys.walletBalance });
    qc.invalidateQueries({ queryKey: queryKeys.investmentSplit });
  };

  const createMutation = useMutation({
    mutationFn: sipService.create,
    onSuccess: () => {
      toast({ title: "SIP Created", description: `SIP of ${fmt(parseFloat(sipAmount))} set up for ${selectedFund}` });
      invalidate();
      closeCreate();
    },
    onError: (err: any) => {
      toast({
        title: "Failed to create SIP",
        description: err?.response?.data?.message || "An error occurred",
        variant: "destructive",
      });
    },
  });

  const cancelMutation = useMutation({
    mutationFn: sipService.cancel,
    onSuccess: () => {
      toast({ title: "SIP Cancelled", description: "SIP has been deactivated" });
      invalidate();
      setCancelId(null);
    },
    onError: () => {
      toast({ title: "Error", description: "Failed to cancel SIP", variant: "destructive" });
    },
  });

  const closeCreate = () => {
    setCreateOpen(false);
    setSelectedFund("");
    setSipAmount("");
    setFrequency("MONTHLY");
  };

  const handleCreate = () => {
    const amount = parseFloat(sipAmount);
    if (!selectedFund || amount < 100) {
      toast({ title: "Validation", description: "Select a fund and enter amount ≥ ₹100", variant: "destructive" });
      return;
    }
    createMutation.mutate({ symbol: selectedFund, amount, frequency });
  };

  const activeSips = sips.filter((s) => s.active);
  const totalSipAmount = activeSips.reduce((s, sip) => s + Number(sip.amount), 0);

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold font-display">SIP Management</h1>
        <Button size="sm" onClick={() => setCreateOpen(true)}>
          <Plus className="h-4 w-4 mr-1" /> New SIP
        </Button>
      </div>

      {/* Summary */}
      {loadingSips ? (
        <CardSkeleton count={2} />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <Card>
            <CardContent className="flex items-center gap-4 p-5">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                <RefreshCw className="h-5 w-5 text-primary" />
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Active SIPs</p>
                <p className="text-lg font-bold">{activeSips.length}</p>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="flex items-center gap-4 p-5">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                <IndianRupee className="h-5 w-5 text-primary" />
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Monthly SIP Total</p>
                <p className="text-lg font-bold">{fmt(totalSipAmount)}</p>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="flex items-center gap-4 p-5">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                <CalendarClock className="h-5 w-5 text-primary" />
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Total SIPs</p>
                <p className="text-lg font-bold">{sips.length}</p>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* SIP List */}
      <Card>
        <CardHeader>
          <CardTitle className="text-sm font-medium">Your SIPs</CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          {loadingSips ? (
            <div className="p-4"><TableSkeleton /></div>
          ) : sips.length === 0 ? (
            <p className="text-center text-muted-foreground py-12">
              No SIPs yet. Create your first SIP to start investing regularly.
            </p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Fund</TableHead>
                  <TableHead className="text-right">Amount</TableHead>
                  <TableHead>Frequency</TableHead>
                  <TableHead>Next Date</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {sips.map((sip) => (
                  <TableRow key={sip.id}>
                    <TableCell>
                      <span className="font-medium text-sm">{sip.symbol}</span>
                      {sip.fundName && (
                        <p className="text-xs text-muted-foreground truncate max-w-[180px]">{sip.fundName}</p>
                      )}
                    </TableCell>
                    <TableCell className="text-right text-sm font-medium tabular-nums">
                      {fmt(sip.amount)}
                    </TableCell>
                    <TableCell>
                      <Badge variant="outline" className="text-xs">{sip.frequency}</Badge>
                    </TableCell>
                    <TableCell className="text-sm">
                      {sip.nextExecutionDate
                        ? new Date(sip.nextExecutionDate).toLocaleDateString("en-IN", { dateStyle: "medium" })
                        : "—"}
                    </TableCell>
                    <TableCell>
                      <Badge variant={sip.active ? "default" : "secondary"} className="text-xs">
                        {sip.active ? "Active" : "Cancelled"}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      {sip.active && (
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-7 w-7 text-destructive"
                          onClick={() => setCancelId(sip.id)}
                        >
                          <Trash2 className="h-3.5 w-3.5" />
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* Create SIP Dialog */}
      <Dialog open={createOpen} onOpenChange={() => closeCreate()}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <CalendarClock className="h-5 w-5 text-primary" />
              Create New SIP
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="space-y-1">
              <Label className="text-xs">Mutual Fund</Label>
              <Select value={selectedFund} onValueChange={setSelectedFund}>
                <SelectTrigger><SelectValue placeholder="Select a fund" /></SelectTrigger>
                <SelectContent>
                  {funds.map((f) => (
                    <SelectItem key={f.symbol} value={f.symbol}>
                      {f.symbol} — {f.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-1">
              <Label className="text-xs">SIP Amount (₹)</Label>
              <div className="relative">
                <IndianRupee className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  type="number"
                  min={100}
                  step={100}
                  placeholder="Min ₹100"
                  value={sipAmount}
                  onChange={(e) => setSipAmount(e.target.value)}
                  className="pl-9"
                />
              </div>
            </div>
            <div className="space-y-1">
              <Label className="text-xs">Frequency</Label>
              <Select value={frequency} onValueChange={setFrequency}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="MONTHLY">Monthly</SelectItem>
                  <SelectItem value="WEEKLY">Weekly</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={closeCreate}>Cancel</Button>
            <Button onClick={handleCreate} disabled={createMutation.isPending}>
              {createMutation.isPending ? "Creating…" : "Create SIP"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Cancel Confirmation */}
      <AlertDialog open={!!cancelId} onOpenChange={() => setCancelId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Cancel SIP?</AlertDialogTitle>
            <AlertDialogDescription>
              This will deactivate the SIP. No further installments will be processed.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Keep Active</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => cancelId && cancelMutation.mutate(cancelId)}
              className="bg-destructive text-destructive-foreground"
              disabled={cancelMutation.isPending}
            >
              {cancelMutation.isPending ? "Cancelling…" : "Cancel SIP"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

export default SipManagement;
