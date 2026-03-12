import { api } from "@/lib/api";
import type { DashboardSummary, Holding, PortfolioAllocation, PortfolioGrowth } from "@/types";

const CHART_COLORS = [
  "#6366f1", "#22c55e", "#f59e0b", "#ef4444", "#3b82f6",
  "#a855f7", "#14b8a6", "#f97316", "#ec4899", "#84cc16",
];

export const portfolioService = {
  getDashboard: async (): Promise<DashboardSummary> => {
    const { data } = await api.get<DashboardSummary>("/api/portfolio/dashboard");
    return data;
  },

  getHoldings: async (): Promise<Holding[]> => {
    const { data } = await api.get<Holding[]>("/api/holdings");
    return data;
  },

  getAllocation: async (): Promise<PortfolioAllocation[]> => {
    const { data } = await api.get<{ sector: string; totalValue: number; percentage: number; stockCount?: number }[]>("/api/portfolio/allocation");
    return data.map((item, i) => ({
      name: item.sector,
      value: Number(item.totalValue),
      percent: Number(item.percentage),
      color: CHART_COLORS[i % CHART_COLORS.length],
    }));
  },

  getGrowth: async (): Promise<PortfolioGrowth[]> => {
    const { data } = await api.get<PortfolioGrowth[]>("/api/portfolio/growth");
    return data;
  },
};
