const trimTrailingSlash = (value: string) => value.replace(/\/+$/, "");

const getWindowOrigin = () =>
  typeof window !== "undefined" ? trimTrailingSlash(window.location.origin) : "";

const resolveBaseUrl = (configuredValue: string | undefined) => {
  const normalized = configuredValue?.trim();

  if (!normalized) {
    return getWindowOrigin();
  }

  if (
    typeof window !== "undefined" &&
    window.location.hostname !== "localhost" &&
    /^http:\/\/localhost:(8081|8082)$/.test(normalized)
  ) {
    return getWindowOrigin();
  }

  return trimTrailingSlash(normalized);
};

export const apiBaseUrl = resolveBaseUrl(import.meta.env.VITE_API_BASE_URL);
export const appInfoBaseUrl = resolveBaseUrl(import.meta.env.VITE_APPINFO_BASE_URL);
