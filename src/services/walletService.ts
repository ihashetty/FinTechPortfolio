import { api } from "@/lib/api";
import type { LedgerEntry } from "@/types";

export const walletService = {
  getBalance: async (): Promise<number> => {
    const { data } = await api.get<number>("/api/wallet/balance");
    return data;
  },

  getLedger: async (): Promise<LedgerEntry[]> => {
    const { data } = await api.get<LedgerEntry[]>("/api/wallet/ledger");
    return data;
  },

  deposit: async (amount: number): Promise<number> => {
    const { data } = await api.post<number>("/api/wallet/deposit", { amount });
    return data;
  },

  withdraw: async (amount: number): Promise<number> => {
    const { data } = await api.post<number>("/api/wallet/withdraw", { amount });
    return data;
  },
};
