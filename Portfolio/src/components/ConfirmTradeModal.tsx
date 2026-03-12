import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";

interface LineItem {
  label: string;
  value: string;
  bold?: boolean;
}

interface ConfirmTradeModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description?: string;
  lines: LineItem[];
  confirmLabel?: string;
  onConfirm: () => void;
  loading?: boolean;
}

export function ConfirmTradeModal({
  open,
  onOpenChange,
  title,
  description,
  lines,
  confirmLabel = "Confirm",
  onConfirm,
  loading = false,
}: ConfirmTradeModalProps) {
  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{title}</AlertDialogTitle>
          {description && (
            <AlertDialogDescription>{description}</AlertDialogDescription>
          )}
        </AlertDialogHeader>

        <div className="space-y-2 py-3 text-sm">
          {lines.map((l) => (
            <div
              key={l.label}
              className={`flex justify-between ${l.bold ? "font-semibold border-t pt-2" : ""}`}
            >
              <span className="text-muted-foreground">{l.label}</span>
              <span>{l.value}</span>
            </div>
          ))}
        </div>

        <AlertDialogFooter>
          <AlertDialogCancel disabled={loading}>Cancel</AlertDialogCancel>
          <AlertDialogAction onClick={onConfirm} disabled={loading}>
            {loading ? "Processing…" : confirmLabel}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
