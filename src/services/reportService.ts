import { getAccessToken } from "@/lib/api";
import { apiBaseUrl } from "@/lib/runtimeConfig";

const downloadFile = async (path: string, filename: string) => {
  const token = getAccessToken();
  const response = await fetch(`${apiBaseUrl}${path}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });

  if (!response.ok) {
    throw new Error(`Download failed: ${response.statusText}`);
  }

  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
};

export const reportService = {
  downloadPortfolioSummary: () =>
    downloadFile("/api/reports/portfolio-summary", "portfolio-summary.csv"),

  downloadTransactions: () =>
    downloadFile("/api/reports/transactions", "transactions.csv"),

  downloadTaxReport: (fy?: string) => {
    const path = fy
      ? `/api/reports/tax-report?fy=${encodeURIComponent(fy)}`
      : "/api/reports/tax-report";
    const filename = fy ? `tax-report-${fy}.csv` : "tax-report.csv";
    return downloadFile(path, filename);
  },
};
