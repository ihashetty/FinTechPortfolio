// --- Auth ---

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  userId: number;
  email: string;
  name: string;
  currency: string;
  darkMode: boolean;
}

// --- User ---

export interface UserProfile {
  userId: number;
  name: string;
  email: string;
  currency: string;
  darkMode: boolean;
}

// --- Stock ---

export interface Stock {
  symbol: string;
  name: string;
  sector: string;
  currentPrice: number;
  lastUpdated: string;
}

// --- Transactions ---

export interface Transaction {
  id: number;
  assetType?: string;
  stockSymbol: string;
  stockName: string;
  type: "BUY" | "SELL";
  quantity: number;
  price: number;
  transactionDate: string; // "yyyy-MM-dd"
  brokerage: number;
  totalAmount?: number;
  notes?: string;
  createdAt?: string;
  orderType?: "MARKET" | "LIMIT";
}

export interface CreateTransactionDTO {
  stockSymbol: string;
  stockName: string;
  type: "BUY" | "SELL";
  quantity: number;
  price?: number; // optional for MARKET orders
  transactionDate: string;
  brokerage: number;
  notes?: string;
  orderType?: "MARKET" | "LIMIT";
}

export type UpdateTransactionDTO = Partial<CreateTransactionDTO>;

// --- Holdings ---

export type AssetType = "STOCK" | "MF";

export interface Holding {
  assetType?: AssetType;
  symbol: string;
  name: string;
  sector?: string;
  quantity: number;
  avgBuyPrice: number;
  investedAmount: number;
  currentPrice: number;
  totalValue: number;
  gainLoss: number;
  returnPercent: number;
  dayChange?: number;
  weightPercent: number;
}

// --- Dashboard ---

export interface DashboardSummary {
  totalInvested: number;
  currentValue: number;
  totalPL: number;
  returnPercent: number;
  xirr: number;
  topGainer: Holding | string | null;
  topLoser: Holding | string | null;
  largestHolding: Holding | string | null;
}

// --- Portfolio ---

export interface PortfolioAllocation {
  name: string;
  value: number;
  percent?: number;
  color: string; // assigned client-side
}

/** Raw shape returned by the backend PortfolioAllocationDTO */
export interface PortfolioAllocationRaw {
  sector: string;
  totalValue: number;
  percentage: number;
  stockCount?: number;
}

export interface PortfolioGrowth {
  date: string;
  totalValue: number;
  totalInvested: number;
  pnl: number;
}

// --- Watchlist ---

export interface WatchlistItem {
  id: number;
  stockSymbol: string;
  stockName: string;
  sector?: string;
  currentPrice?: number;
  dayChange?: number;
  dayChangePercent?: number;
  changePercent?: number;
  addedDate?: string;
  createdAt?: string;
}

// --- Alerts ---

export interface PriceAlert {
  id: number;
  stockSymbol: string;
  stockName: string;
  targetPrice: number;
  currentPrice?: number;
  direction: "ABOVE" | "BELOW";
  active?: boolean;
}

// --- Analytics ---

export interface MonthlyPL {
  month: string;
  monthLabel?: string;
  year?: number;
  monthNumber?: number;
  realisedPL: number;
  unrealisedPL: number;
  invested?: number;
  proceeds?: number;
  /** Computed client-side for charts */
  profit: number;
  loss: number;
}

export interface TaxSummary {
  financialYear: string;
  totalStcgGains: number;
  totalStcgLosses: number;
  netStcg: number;
  stcgTaxRate?: number;
  stcgTaxLiability: number;
  totalLtcgGains: number;
  totalLtcgLosses: number;
  netLtcg: number;
  ltcgExemption?: number;
  taxableLtcg?: number;
  ltcgTaxRate?: number;
  ltcgTaxLiability: number;
  totalTaxLiability: number;
  totalRealised?: number;
  lineItems?: unknown[];
  /** Computed client-side aliases for backward compat */
  fy: string;
  shortTermGains: number;
  longTermGains: number;
  stcgTax: number;
  ltcgTax: number;
}

// --- Wallet ---

export interface LedgerEntry {
  id: number;
  type: string;
  amount: number;
  referenceId?: number;
  createdAt: string;
}

export interface WalletBalance {
  balance: number;
  recentEntries: LedgerEntry[];
}

// --- Mutual Funds ---

export interface MutualFund {
  id: number;
  symbol: string;
  name: string;
  category: string;
  nav: number;
  lastUpdated: string;
}

export interface BuyMutualFundRequest {
  symbol: string;
  amount: number;
}

export interface SellMutualFundRequest {
  symbol: string;
  units: number;
}

// --- SIP ---

export interface SipInstruction {
  id: number;
  symbol: string;
  fundName: string;
  amount: number;
  frequency: string;
  nextExecutionDate: string;
  active: boolean;
  createdAt: string;
}

export interface CreateSipRequest {
  symbol: string;
  amount: number;
  frequency?: string;
}

// --- App Info (from AppInfo microservice) ---

export interface AppInfo {
  appName: string;
  tagline: string;
  version: string;
  supportEmail: string;
  supportPhone: string;
  address: string;
  socialLinks: Record<string, string>;
  copyright: string;
  termsUrl: string;
  privacyUrl: string;
}
