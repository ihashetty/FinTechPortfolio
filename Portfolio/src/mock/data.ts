import type {
  Transaction,
  Holding,
  DashboardSummary,
  PortfolioAllocation,
  PortfolioGrowth,
  WatchlistItem,
  PriceAlert,
  MonthlyPL,
  SectorAllocation,
  UserProfile,
} from "@/types";

export const mockUser: UserProfile = {
  name: "user1",
  email: "user1@example.com",
  currency: "₹",
  darkMode: false,
};

export const mockTransactions: Transaction[] = [
  { id: "t1", stockSymbol: "TCS", stockName: "Tata Consultancy Services", type: "BUY", quantity: 10, price: 3450, date: "2024-01-15", brokerage: 20, notes: "Long-term hold" },
  { id: "t2", stockSymbol: "RELIANCE", stockName: "Reliance Industries", type: "BUY", quantity: 15, price: 2480, date: "2024-02-10", brokerage: 20, notes: "" },
  { id: "t3", stockSymbol: "INFY", stockName: "Infosys", type: "BUY", quantity: 20, price: 1520, date: "2024-03-05", brokerage: 20, notes: "IT sector bet" },
  { id: "t4", stockSymbol: "HDFCBANK", stockName: "HDFC Bank", type: "BUY", quantity: 25, price: 1580, date: "2024-03-20", brokerage: 20, notes: "" },
  { id: "t5", stockSymbol: "WIPRO", stockName: "Wipro", type: "BUY", quantity: 30, price: 450, date: "2024-04-12", brokerage: 20, notes: "" },
  { id: "t6", stockSymbol: "TATAMOTORS", stockName: "Tata Motors", type: "BUY", quantity: 20, price: 980, date: "2024-05-08", brokerage: 20, notes: "EV story" },
  { id: "t7", stockSymbol: "SBIN", stockName: "State Bank of India", type: "BUY", quantity: 35, price: 620, date: "2024-06-15", brokerage: 20, notes: "" },
  { id: "t8", stockSymbol: "ITC", stockName: "ITC Limited", type: "BUY", quantity: 40, price: 440, date: "2024-07-01", brokerage: 20, notes: "Dividend play" },
  { id: "t9", stockSymbol: "WIPRO", stockName: "Wipro", type: "SELL", quantity: 10, price: 510, date: "2024-08-20", brokerage: 20, notes: "Partial profit booking" },
  { id: "t10", stockSymbol: "BAJFINANCE", stockName: "Bajaj Finance", type: "BUY", quantity: 8, price: 6850, date: "2024-09-10", brokerage: 20, notes: "" },
];

export const mockHoldings: Holding[] = [
  { stockSymbol: "TCS", stockName: "Tata Consultancy Services", sector: "IT", quantity: 10, avgBuyPrice: 3450, currentPrice: 3920, totalValue: 39200, gainLoss: 4700, returnPercent: 13.62, weightPercent: 15.2, daysHeld: 380, firstBuyDate: "2024-01-15" },
  { stockSymbol: "RELIANCE", stockName: "Reliance Industries", sector: "Oil & Gas", quantity: 15, avgBuyPrice: 2480, currentPrice: 2650, totalValue: 39750, gainLoss: 2550, returnPercent: 6.85, weightPercent: 15.4, daysHeld: 350, firstBuyDate: "2024-02-10" },
  { stockSymbol: "INFY", stockName: "Infosys", sector: "IT", quantity: 20, avgBuyPrice: 1520, currentPrice: 1440, totalValue: 28800, gainLoss: -1600, returnPercent: -5.26, weightPercent: 11.2, daysHeld: 325, firstBuyDate: "2024-03-05" },
  { stockSymbol: "HDFCBANK", stockName: "HDFC Bank", sector: "Banking", quantity: 25, avgBuyPrice: 1580, currentPrice: 1720, totalValue: 43000, gainLoss: 3500, returnPercent: 8.86, weightPercent: 16.7, daysHeld: 310, firstBuyDate: "2024-03-20" },
  { stockSymbol: "WIPRO", stockName: "Wipro", sector: "IT", quantity: 20, avgBuyPrice: 450, currentPrice: 520, totalValue: 10400, gainLoss: 1400, returnPercent: 15.56, weightPercent: 4.0, daysHeld: 285, firstBuyDate: "2024-04-12" },
  { stockSymbol: "TATAMOTORS", stockName: "Tata Motors", sector: "Auto", quantity: 20, avgBuyPrice: 980, currentPrice: 1050, totalValue: 21000, gainLoss: 1400, returnPercent: 7.14, weightPercent: 8.1, daysHeld: 260, firstBuyDate: "2024-05-08" },
  { stockSymbol: "SBIN", stockName: "State Bank of India", sector: "Banking", quantity: 35, avgBuyPrice: 620, currentPrice: 680, totalValue: 23800, gainLoss: 2100, returnPercent: 9.68, weightPercent: 9.2, daysHeld: 220, firstBuyDate: "2024-06-15" },
  { stockSymbol: "ITC", stockName: "ITC Limited", sector: "FMCG", quantity: 40, avgBuyPrice: 440, currentPrice: 465, totalValue: 18600, gainLoss: 1000, returnPercent: 5.68, weightPercent: 7.2, daysHeld: 205, firstBuyDate: "2024-07-01" },
  { stockSymbol: "BAJFINANCE", stockName: "Bajaj Finance", sector: "NBFC", quantity: 8, avgBuyPrice: 6850, currentPrice: 7200, totalValue: 57600, gainLoss: 2800, returnPercent: 5.11, weightPercent: 13.0, daysHeld: 135, firstBuyDate: "2024-09-10" },
];

export const mockDashboard: DashboardSummary = {
  totalInvested: 240170,
  currentValue: 282150,
  totalPL: 41980,
  returnPercent: 17.48,
  xirr: 14.2,
  topGainer: "WIPRO",
  topLoser: "INFY",
  largestHolding: "HDFCBANK",
};

export const mockAllocation: PortfolioAllocation[] = [
  { name: "IT", value: 78400, color: "hsl(175, 77%, 32%)" },
  { name: "Banking", value: 66800, color: "hsl(142, 71%, 45%)" },
  { name: "Oil & Gas", value: 39750, color: "hsl(38, 92%, 50%)" },
  { name: "Auto", value: 21000, color: "hsl(262, 52%, 47%)" },
  { name: "FMCG", value: 18600, color: "hsl(200, 70%, 50%)" },
  { name: "NBFC", value: 57600, color: "hsl(340, 65%, 50%)" },
];

export const mockGrowth: PortfolioGrowth[] = [
  { date: "Jan 24", value: 34500 },
  { date: "Feb 24", value: 71700 },
  { date: "Mar 24", value: 102100 },
  { date: "Apr 24", value: 115600 },
  { date: "May 24", value: 135200 },
  { date: "Jun 24", value: 156900 },
  { date: "Jul 24", value: 174500 },
  { date: "Aug 24", value: 182300 },
  { date: "Sep 24", value: 237000 },
  { date: "Oct 24", value: 251400 },
  { date: "Nov 24", value: 268900 },
  { date: "Dec 24", value: 275200 },
  { date: "Jan 25", value: 282150 },
];

export const mockWatchlist: WatchlistItem[] = [
  { id: "w1", stockSymbol: "ASIANPAINT", stockName: "Asian Paints", currentPrice: 2850, changePercent: 1.2, addedDate: "2024-10-01" },
  { id: "w2", stockSymbol: "MARUTI", stockName: "Maruti Suzuki", currentPrice: 12400, changePercent: -0.5, addedDate: "2024-10-15" },
  { id: "w3", stockSymbol: "SUNPHARMA", stockName: "Sun Pharma", currentPrice: 1680, changePercent: 2.1, addedDate: "2024-11-01" },
  { id: "w4", stockSymbol: "LTIM", stockName: "LTIMindtree", currentPrice: 5450, changePercent: -1.3, addedDate: "2024-11-20" },
];

export const mockAlerts: PriceAlert[] = [
  { id: "a1", stockSymbol: "TCS", stockName: "TCS", targetPrice: 4000, currentPrice: 3920, direction: "ABOVE", active: true },
  { id: "a2", stockSymbol: "INFY", stockName: "Infosys", targetPrice: 1400, currentPrice: 1440, direction: "BELOW", active: true },
  { id: "a3", stockSymbol: "RELIANCE", stockName: "Reliance", targetPrice: 2800, currentPrice: 2650, direction: "ABOVE", active: false },
];

export const mockMonthlyPL: MonthlyPL[] = [
  { month: "Jan 24", profit: 2100, loss: 0 },
  { month: "Feb 24", profit: 1800, loss: -500 },
  { month: "Mar 24", profit: 3200, loss: -1200 },
  { month: "Apr 24", profit: 1500, loss: -800 },
  { month: "May 24", profit: 2800, loss: -300 },
  { month: "Jun 24", profit: 3500, loss: -600 },
  { month: "Jul 24", profit: 2200, loss: -900 },
  { month: "Aug 24", profit: 1900, loss: -1100 },
  { month: "Sep 24", profit: 4200, loss: -200 },
  { month: "Oct 24", profit: 3100, loss: -700 },
  { month: "Nov 24", profit: 2600, loss: -400 },
  { month: "Dec 24", profit: 1800, loss: -1000 },
];

export const mockSectorAllocation: SectorAllocation[] = [
  { sector: "IT", value: 27.8, color: "hsl(175, 77%, 32%)" },
  { sector: "Banking", value: 23.7, color: "hsl(142, 71%, 45%)" },
  { sector: "NBFC", value: 20.4, color: "hsl(340, 65%, 50%)" },
  { sector: "Oil & Gas", value: 14.1, color: "hsl(38, 92%, 50%)" },
  { sector: "Auto", value: 7.4, color: "hsl(262, 52%, 47%)" },
  { sector: "FMCG", value: 6.6, color: "hsl(200, 70%, 50%)" },
];
