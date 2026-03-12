import { Skeleton } from "@/components/ui/skeleton";

interface LoadingSkeletonProps {
  /** Number of skeleton rows */
  rows?: number;
  /** Height of each row */
  height?: string;
  /** Additional class names */
  className?: string;
}

export function LoadingSkeleton({
  rows = 4,
  height = "h-12",
  className = "",
}: LoadingSkeletonProps) {
  return (
    <div className={`space-y-3 ${className}`}>
      {Array.from({ length: rows }).map((_, i) => (
        <Skeleton key={i} className={`${height} w-full rounded-lg`} />
      ))}
    </div>
  );
}

export function CardSkeleton({ count = 4 }: { count?: number }) {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {Array.from({ length: count }).map((_, i) => (
        <Skeleton key={i} className="h-28 rounded-lg" />
      ))}
    </div>
  );
}

export function TableSkeleton() {
  return (
    <div className="space-y-2">
      <Skeleton className="h-10 w-full rounded" />
      {Array.from({ length: 6 }).map((_, i) => (
        <Skeleton key={i} className="h-12 w-full rounded" />
      ))}
    </div>
  );
}
