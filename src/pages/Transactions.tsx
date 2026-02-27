import { useEffect, useState } from "react";
import { transactionService } from "@/services/transactionService";
import type { Transaction } from "@/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { useToast } from "@/hooks/use-toast";
import { Plus, Pencil, Trash2, Search } from "lucide-react";

const emptyTx: Omit<Transaction, "id"> = { stockSymbol: "", stockName: "", type: "BUY", quantity: 0, price: 0, date: "", brokerage: 20, notes: "" };

const Transactions = () => {
  const { toast } = useToast();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [editing, setEditing] = useState<Transaction | null>(null);
  const [form, setForm] = useState<Omit<Transaction, "id">>(emptyTx);
  const [filter, setFilter] = useState("");

  const load = async () => {
    const data = await transactionService.getAll();
    setTransactions(data);
    setLoading(false);
  };

  useEffect(() => { load(); }, []);

  const openAdd = () => { setEditing(null); setForm(emptyTx); setDialogOpen(true); };
  const openEdit = (tx: Transaction) => { setEditing(tx); setForm(tx); setDialogOpen(true); };

  const handleSave = async () => {
    if (!form.stockSymbol || form.quantity <= 0 || form.price <= 0 || !form.date) {
      toast({ title: "Validation Error", description: "Please fill all required fields with valid values", variant: "destructive" });
      return;
    }
    if (editing) {
      await transactionService.update(editing.id, form);
      toast({ title: "Updated", description: "Transaction updated successfully" });
    } else {
      await transactionService.add(form);
      toast({ title: "Added", description: "Transaction added successfully" });
    }
    setDialogOpen(false);
    load();
  };

  const handleDelete = async () => {
    if (deleteId) {
      await transactionService.delete(deleteId);
      toast({ title: "Deleted", description: "Transaction removed" });
      setDeleteId(null);
      load();
    }
  };

  const filtered = transactions.filter(
    (t) => !filter || t.stockSymbol.toLowerCase().includes(filter.toLowerCase()) || t.stockName.toLowerCase().includes(filter.toLowerCase())
  );

  if (loading) {
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
        <Button onClick={openAdd} size="sm"><Plus className="h-4 w-4 mr-1" /> Add Transaction</Button>
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
                <TableHead className="text-right">Total (₹)</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filtered.length === 0 ? (
                <TableRow><TableCell colSpan={7} className="text-center py-8 text-muted-foreground">No transactions found</TableCell></TableRow>
              ) : (
                filtered.map((tx) => (
                  <TableRow key={tx.id}>
                    <TableCell className="text-sm">{tx.date}</TableCell>
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
                    <TableCell className="text-right text-sm">{tx.quantity}</TableCell>
                    <TableCell className="text-right text-sm">₹{tx.price.toLocaleString("en-IN")}</TableCell>
                    <TableCell className="text-right text-sm font-medium">₹{(tx.quantity * tx.price).toLocaleString("en-IN")}</TableCell>
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
            <DialogTitle>{editing ? "Edit Transaction" : "Add Transaction"}</DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-2">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label className="text-xs">Stock Symbol</Label>
                <Input value={form.stockSymbol} onChange={(e) => setForm({ ...form, stockSymbol: e.target.value.toUpperCase() })} placeholder="TCS" />
              </div>
              <div className="space-y-1">
                <Label className="text-xs">Stock Name</Label>
                <Input value={form.stockName} onChange={(e) => setForm({ ...form, stockName: e.target.value })} placeholder="Tata Consultancy" />
              </div>
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
                <Label className="text-xs">Price (₹)</Label>
                <Input type="number" min={0.01} step={0.01} value={form.price || ""} onChange={(e) => setForm({ ...form, price: Number(e.target.value) })} />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label className="text-xs">Date</Label>
                <Input type="date" value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} />
              </div>
              <div className="space-y-1">
                <Label className="text-xs">Brokerage (₹)</Label>
                <Input type="number" min={0} value={form.brokerage || ""} onChange={(e) => setForm({ ...form, brokerage: Number(e.target.value) })} />
              </div>
            </div>
            <div className="space-y-1">
              <Label className="text-xs">Notes</Label>
              <Input value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} placeholder="Optional notes" />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleSave}>{editing ? "Update" : "Add"}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation */}
      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Transaction?</AlertDialogTitle>
            <AlertDialogDescription>This action cannot be undone.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete} className="bg-destructive text-destructive-foreground">Delete</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

export default Transactions;
