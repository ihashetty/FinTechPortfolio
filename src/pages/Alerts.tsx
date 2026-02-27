import { useEffect, useState } from "react";
import { watchlistService } from "@/services/watchlistService";
import type { PriceAlert } from "@/types";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";
import { Bell, ArrowUp, ArrowDown, Trash2 } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

// FUTURE: Backend notification system

const Alerts = () => {
  const { toast } = useToast();
  const [alerts, setAlerts] = useState<PriceAlert[]>([]);
  const [loading, setLoading] = useState(true);

  const load = async () => { setAlerts(await watchlistService.getAlerts()); setLoading(false); };
  useEffect(() => { load(); }, []);

  const handleRemove = async (id: string) => {
    await watchlistService.removeAlert(id);
    toast({ title: "Alert removed" });
    load();
  };

  if (loading) return <div className="space-y-4"><h1 className="text-2xl font-bold font-display">Price Alerts</h1><Skeleton className="h-64 rounded-lg" /></div>;

  return (
    <div className="space-y-4 animate-fade-in">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold font-display">Price Alerts</h1>
        <Badge variant="outline" className="gap-1"><Bell className="h-3 w-3" /> {alerts.filter((a) => a.active).length} Active</Badge>
      </div>

      <Card>
        <CardHeader>
          <CardDescription className="text-xs">
            {/* FUTURE: Backend notification system */}
            Alerts are placeholder UI — notifications will be enabled with backend integration.
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
              {alerts.map((a) => (
                <TableRow key={a.id}>
                  <TableCell className="font-medium text-sm">{a.stockSymbol}</TableCell>
                  <TableCell>
                    <span className="flex items-center gap-1 text-sm">
                      {a.direction === "ABOVE" ? <ArrowUp className="h-3 w-3 text-profit" /> : <ArrowDown className="h-3 w-3 text-loss" />}
                      {a.direction}
                    </span>
                  </TableCell>
                  <TableCell className="text-right text-sm">₹{a.targetPrice.toLocaleString("en-IN")}</TableCell>
                  <TableCell className="text-right text-sm">₹{a.currentPrice.toLocaleString("en-IN")}</TableCell>
                  <TableCell>
                    <Badge variant={a.active ? "default" : "secondary"} className="text-xs">{a.active ? "Active" : "Inactive"}</Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="icon" className="h-7 w-7 text-destructive" onClick={() => handleRemove(a.id)}>
                      <Trash2 className="h-3 w-3" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
};

export default Alerts;
