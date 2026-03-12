import { useState, useEffect, useRef } from "react";
import { useQuery } from "@tanstack/react-query";
import { stockService } from "@/services/stockService";
import { queryKeys } from "@/lib/queryKeys";
import type { Stock } from "@/types";
import {
  Command,
  CommandInput,
  CommandList,
  CommandEmpty,
  CommandGroup,
  CommandItem,
} from "@/components/ui/command";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ChevronsUpDown, Check, Search } from "lucide-react";
import { cn } from "@/lib/utils";

interface StockPickerProps {
  /** Currently selected stock symbol */
  value: string;
  /** Fired when user selects a stock */
  onSelect: (stock: Stock) => void;
  /** Placeholder when nothing selected */
  placeholder?: string;
  /** Disable the picker */
  disabled?: boolean;
}

const fmt = (v: number) =>
  "₹" + Number(v).toLocaleString("en-IN", { maximumFractionDigits: 2 });

/**
 * Searchable stock picker (Groww/Zerodha-style).
 * Searches by symbol or name with debounced API calls.
 * Built on shadcn Command + Popover.
 */
export function StockPicker({
  value,
  onSelect,
  placeholder = "Select stock...",
  disabled = false,
}: StockPickerProps) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState("");
  const [debouncedSearch, setDebouncedSearch] = useState("");
  const debounceRef = useRef<ReturnType<typeof setTimeout>>();

  // Debounce search input by 300ms
  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      setDebouncedSearch(search);
    }, 300);
    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, [search]);

  // Fetch matching stocks
  const { data: results = [], isFetching } = useQuery({
    queryKey: queryKeys.stockSearch(debouncedSearch),
    queryFn: () => stockService.search(debouncedSearch),
    enabled: open && debouncedSearch.length >= 1,
    staleTime: 30_000, // cache suggestions for 30s
  });

  // Also fetch initially when popover opens with no search (show popular)
  const { data: initialStocks = [] } = useQuery({
    queryKey: queryKeys.stockSearch(""),
    queryFn: () => stockService.search(""),
    enabled: open,
    staleTime: 60_000,
  });

  const displayStocks = debouncedSearch.length >= 1 ? results : initialStocks.slice(0, 10);

  // Fetch the selected stock's details for the display label
  const { data: selectedStock } = useQuery({
    queryKey: queryKeys.stock(value),
    queryFn: () => stockService.getBySymbol(value),
    enabled: !!value,
    staleTime: 60_000,
  });

  const handleSelect = (stock: Stock) => {
    onSelect(stock);
    setOpen(false);
    setSearch("");
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          role="combobox"
          aria-expanded={open}
          disabled={disabled}
          className={cn(
            "w-full justify-between font-normal h-9 text-sm",
            !value && "text-muted-foreground"
          )}
        >
          {value && selectedStock ? (
            <span className="flex items-center gap-2 truncate">
              <span className="font-semibold">{selectedStock.symbol}</span>
              <span className="text-muted-foreground truncate">
                {selectedStock.name}
              </span>
            </span>
          ) : (
            <span className="flex items-center gap-2">
              <Search className="h-3.5 w-3.5" />
              {placeholder}
            </span>
          )}
          <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-[400px] p-0" align="start">
        <Command shouldFilter={false}>
          <CommandInput
            placeholder="Search by symbol or name..."
            value={search}
            onValueChange={setSearch}
          />
          <CommandList>
            {isFetching && debouncedSearch.length >= 1 ? (
              <div className="py-4 text-center text-sm text-muted-foreground">
                Searching...
              </div>
            ) : displayStocks.length === 0 && debouncedSearch.length >= 1 ? (
              <CommandEmpty>No stocks found for "{debouncedSearch}"</CommandEmpty>
            ) : (
              <CommandGroup
                heading={
                  debouncedSearch.length >= 1
                    ? `${displayStocks.length} result${displayStocks.length !== 1 ? "s" : ""}`
                    : "Popular Stocks"
                }
              >
                {displayStocks.map((stock) => (
                  <CommandItem
                    key={stock.symbol}
                    value={stock.symbol}
                    onSelect={() => handleSelect(stock)}
                    className="flex items-center justify-between py-2.5 cursor-pointer"
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <Check
                        className={cn(
                          "h-4 w-4 shrink-0",
                          value === stock.symbol ? "opacity-100" : "opacity-0"
                        )}
                      />
                      <div className="min-w-0">
                        <div className="flex items-center gap-2">
                          <span className="font-semibold text-sm">
                            {stock.symbol}
                          </span>
                          <Badge variant="outline" className="text-[10px] px-1.5 py-0">
                            {stock.sector}
                          </Badge>
                        </div>
                        <p className="text-xs text-muted-foreground truncate">
                          {stock.name}
                        </p>
                      </div>
                    </div>
                    <span className="text-sm font-medium tabular-nums shrink-0 ml-2">
                      {fmt(stock.currentPrice)}
                    </span>
                  </CommandItem>
                ))}
              </CommandGroup>
            )}
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
}
