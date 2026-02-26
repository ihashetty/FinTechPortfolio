export interface Transaction {
  id: string;
  stockSymbol: string;
  stockName: string;
  type: "BUY" | "SELL";
  quantity: number;
  price: number;
  date: string;
  brokerage: number;
  notes: string;
}

export interface Holding {
  stockSymbol: string;
  stockName: string;
  sector: string;
  quantity: number;
  avgBuyPrice: number;
  currentPrice: number;
  totalValue: number;
  gainLoss: number;
  returnPercent: number;
  weightPercent: number;
  daysHeld: number;
  firstBuyDate: string;
}

export interface DashboardSummary {
  totalInvested: number;
  currentValue: number;
  totalPL: number;
  returnPercent: number;
  xirr: number;
  topGainer: string;
  topLoser: string;
  largestHolding: string;
}

export interface PortfolioAllocation {
  name: string;
  value: number;
  color: string;
}

export interface PortfolioGrowth {
  date: string;
  value: number;
}

export interface WatchlistItem {
  id: string;
  stockSymbol: string;
  stockName: string;
  currentPrice: number;
  changePercent: number;
  addedDate: string;
}

export interface PriceAlert {
  id: string;
  stockSymbol: string;
  stockName: string;
  targetPrice: number;
  currentPrice: number;
  direction: "ABOVE" | "BELOW";
  active: boolean;
}

export interface MonthlyPL {
  month: string;
  profit: number;
  loss: number;
}

export interface SectorAllocation {
  sector: string;
  value: number;
  color: string;
}

export interface UserProfile {
  name: string;
  email: string;
  currency: string;
  darkMode: boolean;
}
