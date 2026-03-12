import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { watchlistService } from "@/services/watchlistService";
import { queryKeys } from "@/lib/queryKeys";
import { Card, CardContent, CardHeader, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { StockPicker } from "@/components/StockPicker";
import type { Stock } from "@/types";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";
import { Bell, ArrowUp, ArrowDown, Trash2, Plus } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

const Alerts = () => {
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedStock, setSelectedStock] = useState<Stock | null>(null);
  const [form, setForm] = useState({ targetPrice: "", direction: "ABOVE" as "ABOVE" | "BELOW" });

  const { data: alerts = [], isLoading } = useQuery({
    queryKey: queryKeys.alerts,
    queryFn: watchlistService.getAlerts,
  });

  const addMutation = useMutation({
    mutationFn: watchlistService.addAlert,
    onSuccess: () => {
      toast({ title: "Alert created" });
      setSelectedStock(null);
      setForm({ targetPrice: "", direction: "ABOVE" });
      setDialogOpen(false);
      queryClient.invalidateQueries({ queryKey: queryKeys.alerts });
    },
    onError: () => toast({ title: "Error", description: "Failed to create alert", variant: "destructive" }),
  });

  const removeMutation = useMutation({
    mutationFn: watchlistService.removeAlert,
    onSuccess: () => {
      toast({ title: "Alert removed" });
      queryClient.invalidateQueries({ queryKey: queryKeys.alerts });
    },
    onError: () => toast({ title: "Error", description: "Failed to remove alert", variant: "destructive" }),
  });

  const handleAdd = () => {
    if (!selectedStock || !form.targetPrice) return;
    addMutation.mutate({
      stockSymbol: selectedStock.symbol,
      stockName: selectedStock.name,
      targetPrice: Number(form.targetPrice),
      direction: form.direction,
    });
  };

  if (isLoading) {
    return (
      <div className="space-y-4">
        <h1 className="text-2xl font-bold font-display">Price Alerts</h1>
        <Skeleton className="h-64 rounded-lg" />
      </div>
    );
  }

  const activeCount = alerts.filter((a) => a.active !== false).length;

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold font-display">Price Alerts</h1>
        <div className="flex items-center gap-2">
          <Badge variant="outline" className="gap-1"><Bell className="h-3 w-3" /> {activeCount} Active</Badge>
          <Button size="sm" onClick={() => setDialogOpen(true)}><Plus className="h-4 w-4 mr-1" /> Add Alert</Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardDescription className="text-xs">
            Alerts trigger when a stock crosses your target price.
          </CardDescription>
        </CardHeader>
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Stock</TableHead>
                <TableHead>Direction</TableHead>
                <TableHead className="text-right">Target (₹)</TableHead>
                <TableHead className="text-right">Current (₹)</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {alerts.length === 0 ? (
                <TableRow><TableCell colSpan={6} className="text-center py-8 text-muted-foreground">No alerts configured</TableCell></TableRow>
              ) : alerts.map((a) => (
                <TableRow key={a.id}>
                  <TableCell>
                    <span className="font-medium text-sm">{a.stockSymbol}</span>
                    <p className="text-xs text-muted-foreground">{a.stockName}</p>
                  </TableCell>
                  <TableCell>
                    <span className="flex items-center gap-1 text-sm">
                      {a.direction === "ABOVE" ? <ArrowUp className="h-3 w-3 text-profit" /> : <ArrowDown className="h-3 w-3 text-loss" />}
                      {a.direction}
                    </span>
                  </TableCell>
                  <TableCell className="text-right text-sm">₹{Number(a.targetPrice).toLocaleString("en-IN")}</TableCell>
                  <TableCell className="text-right text-sm">
                    {a.currentPrice != null ? `₹${Number(a.currentPrice).toLocaleString("en-IN")}` : "—"}
                  </TableCell>
                  <TableCell>
                    <Badge variant={a.active !== false ? "default" : "secondary"} className="text-xs">
                      {a.active !== false ? "Active" : "Triggered"}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button
                      variant="ghost" size="icon" className="h-7 w-7 text-destructive"
                      onClick={() => removeMutation.mutate(a.id)}
                      disabled={removeMutation.isPending}
                    >
                      <Trash2 className="h-3 w-3" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {/* Add Alert Dialog */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader><DialogTitle>Create Price Alert</DialogTitle></DialogHeader>
          <div className="grid gap-4 py-2">
            <div className="space-y-1">
              <Label className="text-xs">Stock</Label>
              <StockPicker
                value={selectedStock?.symbol ?? ""}
                onSelect={(stock) => setSelectedStock(stock)}
                placeholder="Search stocks..."
              />
              {selectedStock && (
                <p className="text-xs text-muted-foreground mt-1">
                  {selectedStock.name} — Current: ₹{selectedStock.currentPrice?.toLocaleString("en-IN")}
                </p>
              )}
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label className="text-xs">Target Price (₹)</Label>
                <Input type="number" min={0.01} step={0.01} value={form.targetPrice} onChange={(e) => setForm({ ...form, targetPrice: e.target.value })} placeholder="2500" />
              </div>
              <div className="space-y-1">
                <Label className="text-xs">Direction</Label>
                <Select value={form.direction} onValueChange={(v) => setForm({ ...form, direction: v as "ABOVE" | "BELOW" })}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="ABOVE">ABOVE</SelectItem>
                    <SelectItem value="BELOW">BELOW</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleAdd} disabled={addMutation.isPending}>Create Alert</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default Alerts;
