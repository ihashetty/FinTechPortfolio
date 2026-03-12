import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { analyticsService } from "@/services/analyticsService";
import { reportService } from "@/services/reportService";
import { queryKeys } from "@/lib/queryKeys";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { FileText, Download, FileSpreadsheet, Loader2 } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

const currentFY = (): string => {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth() + 1; // 1-based
  return month >= 4 ? `${year}-${String(year + 1).slice(2)}` : `${year - 1}-${String(year).slice(2)}`;
};

const Reports = () => {
  const { toast } = useToast();
  const fy = currentFY();
  const [downloading, setDownloading] = useState<string | null>(null);

  const { data: taxSummary, isLoading: loadingTax } = useQuery({
    queryKey: queryKeys.taxSummary(fy),
    queryFn: () => analyticsService.getTaxSummary(fy),
  });

  const handleDownload = async (key: string, fn: () => Promise<void>) => {
    setDownloading(key);
    try {
      await fn();
    } catch {
      toast({ title: "Download failed", description: "Could not fetch the report from the server.", variant: "destructive" });
    } finally {
      setDownloading(null);
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <h1 className="text-2xl font-bold font-display">Reports & Export</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <FileText className="h-5 w-5 text-primary" />
              <CardTitle className="text-sm">Portfolio Summary</CardTitle>
            </div>
            <CardDescription className="text-xs">Complete portfolio overview (CSV)</CardDescription>
          </CardHeader>
          <CardContent>
            <Button
              variant="outline" className="w-full"
              disabled={downloading === "portfolio"}
              onClick={() => handleDownload("portfolio", reportService.downloadPortfolioSummary)}
            >
              {downloading === "portfolio" ? <Loader2 className="h-4 w-4 mr-2 animate-spin" /> : <Download className="h-4 w-4 mr-2" />}
              Export CSV
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <FileSpreadsheet className="h-5 w-5 text-primary" />
              <CardTitle className="text-sm">Transaction History</CardTitle>
            </div>
            <CardDescription className="text-xs">All transactions with P&L (CSV)</CardDescription>
          </CardHeader>
          <CardContent>
            <Button
              variant="outline" className="w-full"
              disabled={downloading === "transactions"}
              onClick={() => handleDownload("transactions", reportService.downloadTransactions)}
            >
              {downloading === "transactions" ? <Loader2 className="h-4 w-4 mr-2 animate-spin" /> : <Download className="h-4 w-4 mr-2" />}
              Export CSV
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <FileText className="h-5 w-5 text-primary" />
              <CardTitle className="text-sm">Tax P&L Report</CardTitle>
            </div>
            <CardDescription className="text-xs">Capital gains summary for FY {fy}</CardDescription>
          </CardHeader>
          <CardContent>
            <Button
              variant="outline" className="w-full"
              disabled={downloading === "tax"}
              onClick={() => handleDownload("tax", () => reportService.downloadTaxReport(fy))}
            >
              {downloading === "tax" ? <Loader2 className="h-4 w-4 mr-2 animate-spin" /> : <Download className="h-4 w-4 mr-2" />}
              Export CSV
            </Button>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Tax Summary (FY {fy})</CardTitle>
        </CardHeader>
        <CardContent>
          {loadingTax ? (
            <Skeleton className="h-16 w-full rounded" />
          ) : (
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
              <div>
                <p className="text-xs text-muted-foreground">Short Term Gains</p>
                <p className={`text-lg font-bold ${(taxSummary?.shortTermGains ?? 0) >= 0 ? "text-profit" : "text-loss"}`}>
                  {(taxSummary?.shortTermGains ?? 0) >= 0 ? "+" : ""}₹{Number(taxSummary?.shortTermGains ?? 0).toLocaleString("en-IN")}
                </p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Long Term Gains</p>
                <p className={`text-lg font-bold ${(taxSummary?.longTermGains ?? 0) >= 0 ? "text-profit" : "text-loss"}`}>
                  {(taxSummary?.longTermGains ?? 0) >= 0 ? "+" : ""}₹{Number(taxSummary?.longTermGains ?? 0).toLocaleString("en-IN")}
                </p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">STCG Tax (15%)</p>
                <p className="text-lg font-bold text-foreground">₹{Number(taxSummary?.stcgTax ?? 0).toLocaleString("en-IN")}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">LTCG Tax (10%)</p>
                <p className="text-lg font-bold text-foreground">₹{Number(taxSummary?.ltcgTax ?? 0).toLocaleString("en-IN")}</p>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default Reports;
