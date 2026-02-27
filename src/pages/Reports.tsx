import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { FileText, Download, FileSpreadsheet } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

const Reports = () => {
  const { toast } = useToast();

  const handleExport = (type: string) => {
    // TODO: Connect with backend export API
    toast({ title: "Coming Soon", description: `${type} export will be available with backend integration` });
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
            <CardDescription className="text-xs">Complete portfolio overview in PDF format</CardDescription>
          </CardHeader>
          <CardContent>
            <Button variant="outline" className="w-full" onClick={() => handleExport("PDF Portfolio Summary")}>
              <Download className="h-4 w-4 mr-2" /> Export PDF
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <FileSpreadsheet className="h-5 w-5 text-primary" />
              <CardTitle className="text-sm">Transaction History</CardTitle>
            </div>
            <CardDescription className="text-xs">All transactions in Excel format</CardDescription>
          </CardHeader>
          <CardContent>
            <Button variant="outline" className="w-full" onClick={() => handleExport("Excel Transactions")}>
              <Download className="h-4 w-4 mr-2" /> Export Excel
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <FileText className="h-5 w-5 text-primary" />
              <CardTitle className="text-sm">Tax P&L Report</CardTitle>
            </div>
            <CardDescription className="text-xs">Capital gains summary for tax filing</CardDescription>
          </CardHeader>
          <CardContent>
            <Button variant="outline" className="w-full" onClick={() => handleExport("Tax P&L Report")}>
              <Download className="h-4 w-4 mr-2" /> Export Report
            </Button>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Tax Summary (FY 2024-25)</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
            <div>
              <p className="text-xs text-muted-foreground">Short Term Gains</p>
              <p className="text-lg font-bold text-profit">+₹1,200</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Long Term Gains</p>
              <p className="text-lg font-bold text-profit">+₹0</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground">STCG Tax (15%)</p>
              <p className="text-lg font-bold text-foreground">₹180</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground">LTCG Tax (10%)</p>
              <p className="text-lg font-bold text-foreground">₹0</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default Reports;
