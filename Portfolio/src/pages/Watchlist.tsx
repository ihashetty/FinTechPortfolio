import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { watchlistService } from "@/services/watchlistService";
import { queryKeys } from "@/lib/queryKeys";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";
import { useToast } from "@/hooks/use-toast";
import { Plus, Trash2, TrendingUp, TrendingDown } from "lucide-react";
import { StockPicker } from "@/components/StockPicker";
import type { Stock } from "@/types";

const Watchlist = () => {
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedStock, setSelectedStock] = useState<Stock | null>(null);

  const { data: items = [], isLoading } = useQuery({
    queryKey: queryKeys.watchlist,
    queryFn: watchlistService.getWatchlist,
  });

  const addMutation = useMutation({
    mutationFn: watchlistService.addToWatchlist,
    onSuccess: () => {
      toast({ title: "Added", description: `${selectedStock?.symbol} added to watchlist` });
      setSelectedStock(null); setDialogOpen(false);
      queryClient.invalidateQueries({ queryKey: queryKeys.watchlist });
    },
    onError: () => toast({ title: "Error", description: "Failed to add to watchlist", variant: "destructive" }),
  });

  const removeMutation = useMutation({
    mutationFn: watchlistService.removeFromWatchlist,
    onSuccess: () => {
      toast({ title: "Removed", description: "Stock removed from watchlist" });
      queryClient.invalidateQueries({ queryKey: queryKeys.watchlist });
    },
    onError: () => toast({ title: "Error", description: "Failed to remove from watchlist", variant: "destructive" }),
  });

  const handleAdd = () => {
    if (!selectedStock) return;
    addMutation.mutate({ stockSymbol: selectedStock.symbol, stockName: selectedStock.name });
  };

  if (isLoading) {
    return (
      <div className="space-y-4">
        <h1 className="text-2xl font-bold font-display">Watchlist</h1>
        <Skeleton className="h-64 rounded-lg" />
      </div>
    );
  }

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold font-display">Watchlist</h1>
        <Button size="sm" onClick={() => setDialogOpen(true)}><Plus className="h-4 w-4 mr-1" /> Add Stock</Button>
      </div>

      <Card>
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Stock</TableHead>
                <TableHead className="text-right">Price (₹)</TableHead>
                <TableHead className="text-right">Change %</TableHead>
                <TableHead className="text-right">Added</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {items.length === 0 ? (
                <TableRow><TableCell colSpan={5} className="text-center py-8 text-muted-foreground">Your watchlist is empty</TableCell></TableRow>
              ) : items.map((w) => (
                <TableRow key={w.id}>
                  <TableCell>
                    <span className="font-medium text-sm">{w.stockSymbol}</span>
                    <p className="text-xs text-muted-foreground">{w.stockName}</p>
                  </TableCell>
                  <TableCell className="text-right text-sm">
                    {w.currentPrice != null ? `₹${Number(w.currentPrice).toLocaleString("en-IN")}` : "—"}
                  </TableCell>
                  <TableCell className="text-right">
                    {w.changePercent != null ? (
                      <span className={`text-sm font-medium flex items-center justify-end gap-1 ${w.changePercent >= 0 ? "text-profit" : "text-loss"}`}>
                        {w.changePercent >= 0 ? <TrendingUp className="h-3 w-3" /> : <TrendingDown className="h-3 w-3" />}
                        {w.changePercent >= 0 ? "+" : ""}{w.changePercent}%
                      </span>
                    ) : "—"}
                  </TableCell>
                  <TableCell className="text-right text-sm text-muted-foreground">{w.addedDate ?? "—"}</TableCell>
                  <TableCell className="text-right">
                    <Button
                      variant="ghost" size="icon" className="h-7 w-7 text-destructive"
                      onClick={() => removeMutation.mutate(w.id)}
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

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader><DialogTitle>Add to Watchlist</DialogTitle></DialogHeader>
          <div className="space-y-3">
            <StockPicker
              value={selectedStock?.symbol ?? ""}
              onSelect={(stock) => setSelectedStock(stock)}
              placeholder="Search stocks..."
            />
            {selectedStock && (
              <p className="text-xs text-muted-foreground">
                {selectedStock.name} — ₹{selectedStock.currentPrice?.toLocaleString("en-IN")}
              </p>
            )}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleAdd} disabled={addMutation.isPending}>Add</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default Watchlist;
