import axios from "axios";
import type { AppInfo } from "@/types";

const appInfoApi = axios.create({
  baseURL: import.meta.env.VITE_APPINFO_BASE_URL || "http://localhost:8082",
  headers: { "Content-Type": "application/json" },
});

export const appInfoService = {
  getAppInfo: async (): Promise<AppInfo> => {
    const { data } = await appInfoApi.get<AppInfo>("/api/app-info");
    return data;
  },
};
