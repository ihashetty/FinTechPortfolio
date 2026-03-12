import { api } from "@/lib/api";
import type {
  MutualFund,
  Holding,
  BuyMutualFundRequest,
  SellMutualFundRequest,
} from "@/types";

export const mutualFundService = {
  getAll: async (): Promise<MutualFund[]> => {
    const { data } = await api.get<MutualFund[]>("/api/mutual-funds");
    return data;
  },

  getHoldings: async (): Promise<Holding[]> => {
    const { data } = await api.get<Holding[]>("/api/mutual-funds/holdings");
    return data;
  },

  buy: async (req: BuyMutualFundRequest): Promise<string> => {
    const { data } = await api.post<string>("/api/mutual-funds/buy", req);
    return data;
  },

  sell: async (req: SellMutualFundRequest): Promise<string> => {
    const { data } = await api.post<string>("/api/mutual-funds/sell", req);
    return data;
  },
};
