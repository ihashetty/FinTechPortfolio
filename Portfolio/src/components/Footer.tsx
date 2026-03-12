import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "@/lib/queryKeys";
import { appInfoService } from "@/services/appInfoService";
import { Mail, Phone, MapPin } from "lucide-react";

export function Footer() {
  const { data: appInfo } = useQuery({
    queryKey: queryKeys.appInfo,
    queryFn: appInfoService.getAppInfo,
    staleTime: 1000 * 60 * 60, // cache for 1 hour
    retry: 1,
  });

  if (!appInfo) {
    return null;
  }

  return (
    <footer className="border-t bg-card text-card-foreground mt-auto">
      <div className="px-4 md:px-6 py-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Brand */}
          <div>
            <h3 className="text-lg font-semibold">{appInfo.appName}</h3>
            <p className="text-sm text-muted-foreground mt-1">
              {appInfo.tagline}
            </p>
            <p className="text-xs text-muted-foreground mt-2">
              v{appInfo.version}
            </p>
          </div>

          {/* Contact */}
          <div className="space-y-2">
            <h4 className="text-sm font-medium">Contact Us</h4>
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Mail className="h-4 w-4 shrink-0" />
              <a
                href={`mailto:${appInfo.supportEmail}`}
                className="hover:underline"
              >
                {appInfo.supportEmail}
              </a>
            </div>
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Phone className="h-4 w-4 shrink-0" />
              <a
                href={`tel:${appInfo.supportPhone}`}
                className="hover:underline"
              >
                {appInfo.supportPhone}
              </a>
            </div>
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <MapPin className="h-4 w-4 shrink-0" />
              <span>{appInfo.address}</span>
            </div>
          </div>

          {/* Links */}
          <div className="space-y-2">
            <h4 className="text-sm font-medium">Links</h4>
            <div className="flex flex-wrap gap-3 text-sm text-muted-foreground">
              {appInfo.socialLinks &&
                Object.entries(appInfo.socialLinks).map(([name, url]) => (
                  <a
                    key={name}
                    href={url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="capitalize hover:underline"
                  >
                    {name}
                  </a>
                ))}
            </div>
            <div className="flex gap-3 text-xs text-muted-foreground mt-2">
              <a
                href={appInfo.termsUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="hover:underline"
              >
                Terms
              </a>
              <a
                href={appInfo.privacyUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="hover:underline"
              >
                Privacy
              </a>
            </div>
          </div>
        </div>

        {/* Copyright */}
        <div className="mt-6 pt-4 border-t text-center text-xs text-muted-foreground">
          {appInfo.copyright}
        </div>
      </div>
    </footer>
  );
}
