import { cn } from "@/lib/utils";
import { TrendingUp, TrendingDown, Minus } from "lucide-react";

interface PriceBadgeProps {
  value: number;
  suffix?: string;
  showIcon?: boolean;
  className?: string;
}

export function PriceBadge({
  value,
  suffix = "",
  showIcon = true,
  className,
}: PriceBadgeProps) {
  const isPositive = value > 0;
  const isNegative = value < 0;
  const isZero = value === 0;

  const Icon = isPositive ? TrendingUp : isNegative ? TrendingDown : Minus;
  const colorClass = isPositive
    ? "text-profit bg-profit/10"
    : isNegative
      ? "text-loss bg-loss/10"
      : "text-muted-foreground bg-secondary";

  return (
    <span
      className={cn(
        "inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium",
        colorClass,
        className,
      )}
    >
      {showIcon && <Icon className="h-3 w-3" />}
      {isPositive && "+"}
      {isZero ? "0.00" : value.toFixed(2)}
      {suffix}
    </span>
  );
}
