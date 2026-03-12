import { api } from "@/lib/api";
import type { Transaction, CreateTransactionDTO, UpdateTransactionDTO } from "@/types";

export const transactionService = {
  getAll: async (symbol?: string): Promise<Transaction[]> => {
    const params = symbol ? { symbol } : {};
    const { data } = await api.get<Transaction[]>("/api/transactions", { params });
    return data;
  },

  getById: async (id: number): Promise<Transaction> => {
    const { data } = await api.get<Transaction>(`/api/transactions/${id}`);
    return data;
  },

  add: async (tx: CreateTransactionDTO): Promise<Transaction> => {
    const { data } = await api.post<Transaction>("/api/transactions", tx);
    return data;
  },

  update: async (id: number, tx: UpdateTransactionDTO): Promise<Transaction> => {
    const { data } = await api.put<Transaction>(`/api/transactions/${id}`, tx);
    return data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/api/transactions/${id}`);
  },
};
