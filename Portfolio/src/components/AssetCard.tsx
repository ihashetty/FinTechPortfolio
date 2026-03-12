import { Card, CardContent } from "@/components/ui/card";
import { PriceBadge } from "@/components/PriceBadge";

interface AssetCardProps {
  symbol: string;
  name: string;
  assetType?: "STOCK" | "MF";
  currentPrice: number;
  changePercent?: number;
  quantity?: number;
  investedValue?: number;
  currentValue?: number;
  onClick?: () => void;
}

const formatCurrency = (val: number) =>
  "₹" + val.toLocaleString("en-IN", { maximumFractionDigits: 2 });

export function AssetCard({
  symbol,
  name,
  assetType,
  currentPrice,
  changePercent,
  quantity,
  investedValue,
  currentValue,
  onClick,
}: AssetCardProps) {
  const pnl =
    investedValue !== undefined && currentValue !== undefined
      ? currentValue - investedValue
      : undefined;

  return (
    <Card
      className={`transition-shadow hover:shadow-md ${onClick ? "cursor-pointer" : ""}`}
      onClick={onClick}
    >
      <CardContent className="p-4 space-y-3">
        <div className="flex items-start justify-between">
          <div className="min-w-0">
            <div className="flex items-center gap-2">
              <span className="font-semibold text-sm truncate">{symbol}</span>
              {assetType && (
                <span className="text-[10px] font-medium px-1.5 py-0.5 rounded bg-secondary text-muted-foreground">
                  {assetType}
                </span>
              )}
            </div>
            <p className="text-xs text-muted-foreground truncate">{name}</p>
          </div>
          <PriceBadge value={changePercent ?? 0} suffix="%" />
        </div>

        <div className="flex items-end justify-between">
          <span className="text-lg font-bold">{formatCurrency(currentPrice)}</span>
          {quantity !== undefined && (
            <span className="text-xs text-muted-foreground">
              {quantity} {assetType === "MF" ? "units" : "shares"}
            </span>
          )}
        </div>

        {pnl !== undefined && (
          <div className="flex items-center justify-between pt-2 border-t text-xs">
            <span className="text-muted-foreground">
              Invested: {formatCurrency(investedValue!)}
            </span>
            <span className={pnl >= 0 ? "text-profit font-medium" : "text-loss font-medium"}>
              {pnl >= 0 ? "+" : ""}
              {formatCurrency(pnl)}
            </span>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
