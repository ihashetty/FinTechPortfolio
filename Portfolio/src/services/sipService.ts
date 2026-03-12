import { api } from "@/lib/api";
import type { SipInstruction, CreateSipRequest } from "@/types";

export const sipService = {
  getAll: async (): Promise<SipInstruction[]> => {
    const { data } = await api.get<SipInstruction[]>("/api/sip");
    return data;
  },

  create: async (req: CreateSipRequest): Promise<SipInstruction> => {
    const { data } = await api.post<SipInstruction>("/api/sip", req);
    return data;
  },

  cancel: async (sipId: number): Promise<void> => {
    await api.delete(`/api/sip/${sipId}`);
  },
};
