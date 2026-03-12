import { api } from "@/lib/api";
import type { WatchlistItem, PriceAlert } from "@/types";

export const watchlistService = {
  getWatchlist: async (): Promise<WatchlistItem[]> => {
    const { data } = await api.get<WatchlistItem[]>("/api/watchlist");
    return data;
  },

  addToWatchlist: async (item: { stockSymbol: string; stockName: string }): Promise<WatchlistItem> => {
    const { data } = await api.post<WatchlistItem>("/api/watchlist", item);
    return data;
  },

  removeFromWatchlist: async (id: number): Promise<void> => {
    await api.delete(`/api/watchlist/${id}`);
  },

  getAlerts: async (): Promise<PriceAlert[]> => {
    const { data } = await api.get<PriceAlert[]>("/api/alerts");
    return data;
  },

  addAlert: async (alert: {
    stockSymbol: string;
    stockName: string;
    targetPrice: number;
    direction: "ABOVE" | "BELOW";
  }): Promise<PriceAlert> => {
    const { data } = await api.post<PriceAlert>("/api/alerts", alert);
    return data;
  },

  removeAlert: async (id: number): Promise<void> => {
    await api.delete(`/api/alerts/${id}`);
  },
};
