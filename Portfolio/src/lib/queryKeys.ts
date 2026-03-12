export const queryKeys = {
  dashboard: ["dashboard"] as const,
  holdings: ["holdings"] as const,
  allocation: ["allocation"] as const,
  growth: ["growth"] as const,
  transactions: (symbol?: string) =>
    symbol ? (["transactions", symbol] as const) : (["transactions"] as const),
  watchlist: ["watchlist"] as const,
  alerts: ["alerts"] as const,
  monthlyPL: ["analytics", "monthlyPL"] as const,
  sectorAllocation: ["analytics", "sector"] as const,
  taxSummary: (fy?: string) =>
    fy ? (["analytics", "taxSummary", fy] as const) : (["analytics", "taxSummary"] as const),
  userProfile: ["userProfile"] as const,
  walletBalance: ["wallet", "balance"] as const,
  walletLedger: ["wallet", "ledger"] as const,
  mutualFunds: ["mutual-funds"] as const,
  mutualFundHoldings: ["mutual-funds", "holdings"] as const,
  sips: ["sip"] as const,
  stocks: ["stocks"] as const,
  stockSearch: (query: string) => ["stocks", "search", query] as const,
  stock: (symbol: string) => ["stocks", symbol] as const,
  appInfo: ["appInfo"] as const,
};
