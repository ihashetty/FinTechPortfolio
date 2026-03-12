import { api } from "@/lib/api";
import type { Stock } from "@/types";

export const stockService = {
  /**
   * Search stocks by symbol or name.
   * Example: search("hd") → HDFCBANK, HDFCLIFE, ...
   */
  search: async (query: string): Promise<Stock[]> => {
    const { data } = await api.get<Stock[]>("/api/stocks/search", {
      params: { q: query },
    });
    return data;
  },

  /**
   * Get a single stock by symbol.
   */
  getBySymbol: async (symbol: string): Promise<Stock> => {
    const { data } = await api.get<Stock>(`/api/stocks/${symbol}`);
    return data;
  },

  /**
   * List all stocks.
   */
  getAll: async (): Promise<Stock[]> => {
    const { data } = await api.get<Stock[]>("/api/stocks");
    return data;
  },
};
