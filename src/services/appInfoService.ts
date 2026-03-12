import axios from "axios";
import { appInfoBaseUrl } from "@/lib/runtimeConfig";
import type { AppInfo } from "@/types";

const appInfoApi = axios.create({
  baseURL: appInfoBaseUrl,
  headers: { "Content-Type": "application/json" },
});

export const appInfoService = {
  getAppInfo: async (): Promise<AppInfo> => {
    const { data } = await appInfoApi.get<AppInfo>("/api/app-info");
    return data;
  },
};
