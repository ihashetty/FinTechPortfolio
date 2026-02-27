// FUTURE: Integrate with Spring Boot backend
// Endpoints: /api/watchlist (GET, POST, DELETE)

import { mockWatchlist, mockAlerts } from "@/mock/data";
import type { WatchlistItem, PriceAlert } from "@/types";

let watchlist = [...mockWatchlist];
let alerts = [...mockAlerts];

export const watchlistService = {
  getWatchlist: async (): Promise<WatchlistItem[]> => {
    // FUTURE: Replace with axios.get("/api/watchlist")
    await new Promise((r) => setTimeout(r, 200));
    return [...watchlist];
  },

  addToWatchlist: async (item: Omit<WatchlistItem, "id">): Promise<WatchlistItem> => {
    // FUTURE: Replace with axios.post("/api/watchlist", item)
    await new Promise((r) => setTimeout(r, 200));
    const newItem: WatchlistItem = { ...item, id: "w" + Date.now() };
    watchlist = [newItem, ...watchlist];
    return newItem;
  },

  removeFromWatchlist: async (id: string): Promise<void> => {
    // FUTURE: Replace with axios.delete(`/api/watchlist/${id}`)
    await new Promise((r) => setTimeout(r, 200));
    watchlist = watchlist.filter((w) => w.id !== id);
  },

  getAlerts: async (): Promise<PriceAlert[]> => {
    // FUTURE: Replace with axios.get("/api/alerts")
    await new Promise((r) => setTimeout(r, 200));
    return [...alerts];
  },

  addAlert: async (alert: Omit<PriceAlert, "id">): Promise<PriceAlert> => {
    // FUTURE: Replace with axios.post("/api/alerts", alert)
    await new Promise((r) => setTimeout(r, 200));
    const newAlert: PriceAlert = { ...alert, id: "a" + Date.now() };
    alerts = [newAlert, ...alerts];
    return newAlert;
  },

  removeAlert: async (id: string): Promise<void> => {
    // FUTURE: Replace with axios.delete(`/api/alerts/${id}`)
    await new Promise((r) => setTimeout(r, 200));
    alerts = alerts.filter((a) => a.id !== id);
  },
};
