import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "@/lib/queryKeys";
import { appInfoService } from "@/services/appInfoService";
import { Mail, Phone, MapPin, Info } from "lucide-react";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet";

export function Footer() {
  const [open, setOpen] = useState(false);
  const { data: appInfo } = useQuery({
    queryKey: queryKeys.appInfo,
    queryFn: appInfoService.getAppInfo,
    staleTime: 1000 * 60 * 60,
    retry: 1,
  });

  return (
    <footer className="border-t bg-card text-card-foreground mt-auto">
      <div className="flex items-center justify-center gap-3 px-4 py-2 text-xs text-muted-foreground">
        <span>{appInfo?.copyright ?? "© 2026 NiveshTrack. All rights reserved."}</span>
        <span className="text-border">|</span>
        <span>v{appInfo?.version ?? "1.0.0"}</span>
        {appInfo && (
          <>
            <span className="text-border">|</span>
            <Sheet open={open} onOpenChange={setOpen}>
              <SheetTrigger asChild>
                <button className="inline-flex items-center gap-1 hover:text-foreground transition-colors cursor-pointer">
                  <Info className="h-3 w-3" />
                  About Us
                </button>
              </SheetTrigger>
              <SheetContent side="bottom" className="max-h-[50vh]">
                <SheetHeader>
                  <SheetTitle>{appInfo.appName}</SheetTitle>
                </SheetHeader>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 py-4">
                  {/* Brand */}
                  <div>
                    <h4 className="text-sm font-medium mb-2">About</h4>
                    <p className="text-sm text-muted-foreground">{appInfo.tagline}</p>
                    <p className="text-xs text-muted-foreground mt-1">Version {appInfo.version}</p>
                  </div>

                  {/* Contact */}
                  <div className="space-y-2">
                    <h4 className="text-sm font-medium">Contact Us</h4>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Mail className="h-4 w-4 shrink-0" />
                      <a href={`mailto:${appInfo.supportEmail}`} className="hover:underline">
                        {appInfo.supportEmail}
                      </a>
                    </div>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Phone className="h-4 w-4 shrink-0" />
                      <a href={`tel:${appInfo.supportPhone}`} className="hover:underline">
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
                      <a href={appInfo.termsUrl} target="_blank" rel="noopener noreferrer" className="hover:underline">
                        Terms of Service
                      </a>
                      <a href={appInfo.privacyUrl} target="_blank" rel="noopener noreferrer" className="hover:underline">
                        Privacy Policy
                      </a>
                    </div>
                  </div>
                </div>
              </SheetContent>
            </Sheet>
          </>
        )}
      </div>
    </footer>
  );
}
