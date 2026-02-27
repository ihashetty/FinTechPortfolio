// FUTURE: Integrate with Spring Boot backend
// Endpoints: /api/transactions (GET, POST, PUT, DELETE)
// FUTURE: Add userId from JWT token

import { mockTransactions } from "@/mock/data";
import type { Transaction } from "@/types";

let transactions = [...mockTransactions];

export const transactionService = {
  getAll: async (): Promise<Transaction[]> => {
    // FUTURE: Replace with axios.get("/api/transactions")
    await new Promise((r) => setTimeout(r, 300));
    return [...transactions];
  },

  add: async (tx: Omit<Transaction, "id">): Promise<Transaction> => {
    // FUTURE: Replace with axios.post("/api/transactions", tx)
    await new Promise((r) => setTimeout(r, 200));
    const newTx: Transaction = { ...tx, id: "t" + Date.now() };
    transactions = [newTx, ...transactions];
    return newTx;
  },

  update: async (id: string, tx: Partial<Transaction>): Promise<Transaction> => {
    // FUTURE: Replace with axios.put(`/api/transactions/${id}`, tx)
    await new Promise((r) => setTimeout(r, 200));
    transactions = transactions.map((t) => (t.id === id ? { ...t, ...tx } : t));
    return transactions.find((t) => t.id === id)!;
  },

  delete: async (id: string): Promise<void> => {
    // FUTURE: Replace with axios.delete(`/api/transactions/${id}`)
    await new Promise((r) => setTimeout(r, 200));
    transactions = transactions.filter((t) => t.id !== id);
  },
};
