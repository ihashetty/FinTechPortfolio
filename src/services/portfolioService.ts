// FUTURE: Integrate with Spring Boot backend
// Endpoints: /api/dashboard, /api/holdings, /api/analytics
// Method: GET
// FUTURE: Add userId from JWT token

import {
  mockDashboard,
  mockHoldings,
  mockAllocation,
  mockGrowth,
  mockMonthlyPL,
  mockSectorAllocation,
} from "@/mock/data";
import type { DashboardSummary, Holding, PortfolioAllocation, PortfolioGrowth, MonthlyPL, SectorAllocation } from "@/types";

export const portfolioService = {
  getDashboard: async (): Promise<DashboardSummary> => {
    // FUTURE: Replace with axios.get("/api/dashboard")
    await new Promise((r) => setTimeout(r, 300));
    return mockDashboard;
  },

  getHoldings: async (): Promise<Holding[]> => {
    // FUTURE: Replace with axios.get("/api/holdings")
    await new Promise((r) => setTimeout(r, 300));
    return mockHoldings;
  },

  getAllocation: async (): Promise<PortfolioAllocation[]> => {
    // FUTURE: Replace with axios.get("/api/allocation")
    await new Promise((r) => setTimeout(r, 200));
    return mockAllocation;
  },

  getGrowth: async (): Promise<PortfolioGrowth[]> => {
    // FUTURE: Replace with axios.get("/api/growth")
    await new Promise((r) => setTimeout(r, 200));
    return mockGrowth;
  },

  getMonthlyPL: async (): Promise<MonthlyPL[]> => {
    // FUTURE: Replace with axios.get("/api/analytics/monthly-pl")
    await new Promise((r) => setTimeout(r, 200));
    return mockMonthlyPL;
  },

  getSectorAllocation: async (): Promise<SectorAllocation[]> => {
    // FUTURE: Replace with axios.get("/api/analytics/sector")
    await new Promise((r) => setTimeout(r, 200));
    return mockSectorAllocation;
  },
};
