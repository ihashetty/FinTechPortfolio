import { api } from "@/lib/api";
import type { MonthlyPL, PortfolioAllocation, TaxSummary } from "@/types";

const SECTOR_COLORS = [
  "#6366f1", "#22c55e", "#f59e0b", "#ef4444", "#3b82f6",
  "#a855f7", "#14b8a6", "#f97316", "#ec4899", "#84cc16",
];

export const analyticsService = {
  getMonthlyPL: async (): Promise<MonthlyPL[]> => {
    const { data } = await api.get<MonthlyPL[]>("/api/analytics/monthly-pl");
    return data.map((d) => ({
      ...d,
      profit: Math.max(0, Number(d.realisedPL ?? 0)),
      loss: Math.min(0, Number(d.realisedPL ?? 0)),
    }));
  },

  getSectorAllocation: async (): Promise<PortfolioAllocation[]> => {
    const { data } = await api.get<{ sector: string; totalValue: number; percentage: number; stockCount?: number }[]>("/api/analytics/sector");
    return data.map((item, i) => ({
      name: item.sector,
      value: Number(item.percentage),
      percent: Number(item.percentage),
      color: SECTOR_COLORS[i % SECTOR_COLORS.length],
    }));
  },

  getTaxSummary: async (fy?: string): Promise<TaxSummary> => {
    const params = fy ? { fy } : {};
    const { data } = await api.get<TaxSummary>("/api/analytics/tax-summary", { params });
    return {
      ...data,
      fy: data.financialYear,
      shortTermGains: Number(data.netStcg ?? 0),
      longTermGains: Number(data.netLtcg ?? 0),
      stcgTax: Number(data.stcgTaxLiability ?? 0),
      ltcgTax: Number(data.ltcgTaxLiability ?? 0),
    };
  },
};
