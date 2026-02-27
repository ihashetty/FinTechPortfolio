import { useEffect, useState } from "react";
import { watchlistService } from "@/services/watchlistService";
import type { WatchlistItem } from "@/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";
import { useToast } from "@/hooks/use-toast";
import { Plus, Trash2, TrendingUp, TrendingDown } from "lucide-react";

const Watchlist = () => {
  const { toast } = useToast();
  const [items, setItems] = useState<WatchlistItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [symbol, setSymbol] = useState("");
  const [name, setName] = useState("");

  const load = async () => { setItems(await watchlistService.getWatchlist()); setLoading(false); };
  useEffect(() => { load(); }, []);

  const handleAdd = async () => {
    if (!symbol) return;
    await watchlistService.addToWatchlist({ stockSymbol: symbol.toUpperCase(), stockName: name, currentPrice: Math.round(Math.random() * 5000 + 500), changePercent: Math.round((Math.random() * 6 - 3) * 100) / 100, addedDate: new Date().toISOString().slice(0, 10) });
    toast({ title: "Added", description: `${symbol.toUpperCase()} added to watchlist` });
    setSymbol(""); setName(""); setDialogOpen(false); load();
  };

  const handleRemove = async (id: string) => {
    await watchlistService.removeFromWatchlist(id);
    toast({ title: "Removed", description: "Stock removed from watchlist" });
    load();
  };

  if (loading) return <div className="space-y-4"><h1 className="text-2xl font-bold font-display">Watchlist</h1><Skeleton className="h-64 rounded-lg" /></div>;

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
                  <TableCell className="text-right text-sm">₹{w.currentPrice.toLocaleString("en-IN")}</TableCell>
                  <TableCell className="text-right">
                    <span className={`text-sm font-medium flex items-center justify-end gap-1 ${w.changePercent >= 0 ? "text-profit" : "text-loss"}`}>
                      {w.changePercent >= 0 ? <TrendingUp className="h-3 w-3" /> : <TrendingDown className="h-3 w-3" />}
                      {w.changePercent >= 0 ? "+" : ""}{w.changePercent}%
                    </span>
                  </TableCell>
                  <TableCell className="text-right text-sm text-muted-foreground">{w.addedDate}</TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="icon" className="h-7 w-7 text-destructive" onClick={() => handleRemove(w.id)}>
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
            <div className="space-y-1"><Label className="text-xs">Symbol</Label><Input value={symbol} onChange={(e) => setSymbol(e.target.value)} placeholder="ASIANPAINT" /></div>
            <div className="space-y-1"><Label className="text-xs">Company Name</Label><Input value={name} onChange={(e) => setName(e.target.value)} placeholder="Asian Paints" /></div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleAdd}>Add</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default Watchlist;
