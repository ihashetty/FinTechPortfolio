# NiveshTrack API Endpoints

Base URL: `http://localhost:8081`

All secured endpoints require the header:
```
Authorization: Bearer <accessToken>
```

---

## Authentication

### POST /api/auth/register
Register a new user.

**Request**
```json
{
  "name": "Arjun Sharma",
  "email": "arjun@example.com",
  "password": "securePass123"
}
```

**Response 201 Created**
```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "550e8400-e29b...",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "arjun@example.com",
  "name": "Arjun Sharma",
  "currency": "INR",
  "darkMode": false
}
```

**Errors:** `400` validation, `409` email already exists

---

### POST /api/auth/login
Login with email and password.

**Request**
```json
{
  "email": "arjun@example.com",
  "password": "securePass123"
}
```

**Response 200 OK** — same structure as register response

**Errors:** `401` bad credentials

---

### POST /api/auth/refresh
Exchange a refresh token for a new access token.

**Request**
```json
{
  "refreshToken": "550e8400-e29b..."
}
```

**Response 200 OK**
```json
{
  "accessToken": "eyJhbGci...",
  "tokenType": "Bearer"
}
```

**Errors:** `401` invalid/expired refresh token

---

## Transactions

### GET /api/transactions
List all transactions for the authenticated user, sorted by date descending.

**Query Params**
| Param | Type | Description |
|---|---|---|
| `symbol` | string | Filter by stock symbol (optional) |

**Response 200 OK**
```json
[
  {
    "id": 10,
    "stockSymbol": "TCS",
    "stockName": "Tata Consultancy Services Ltd",
    "type": "BUY",
    "quantity": 20,
    "price": 3200.00,
    "transactionDate": "2023-06-01",
    "brokerage": 20.00,
    "notes": "First buy",
    "totalAmount": 64000.00,
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

---

### POST /api/transactions
Create a new transaction.

**Request**
```json
{
  "stockSymbol": "RELIANCE",
  "stockName": "Reliance Industries Ltd",
  "type": "BUY",
  "quantity": 10,
  "price": 2500.00,
  "transactionDate": "2024-01-15",
  "brokerage": 20.00,
  "notes": "Added to portfolio"
}
```
`type` values: `BUY` | `SELL`

**Response 201 Created** — TransactionDTO (same as list item)

**Errors:** `400` validation, `422` insufficient quantity for SELL

---

### GET /api/transactions/{id}
Get a specific transaction by ID.

**Response 200 OK** — TransactionDTO

**Errors:** `404` not found

---

### PUT /api/transactions/{id}
Partially update a transaction. All fields optional.

**Request**
```json
{
  "price": 2600.00,
  "notes": "Updated price"
}
```

**Response 200 OK** — updated TransactionDTO

**Errors:** `404` not found

---

### DELETE /api/transactions/{id}
Delete a transaction.

**Response 204 No Content**

**Errors:** `404` not found

---

## Holdings

### GET /api/holdings
Get current holdings (active positions only, net quantity > 0).

**Response 200 OK**
```json
[
  {
    "symbol": "TCS",
    "name": "Tata Consultancy Services Ltd",
    "sector": "Information Technology",
    "quantity": 30,
    "avgBuyPrice": 3226.67,
    "investedAmount": 96800.00,
    "currentPrice": 3950.00,
    "totalValue": 118500.00,
    "gainLoss": 21700.00,
    "returnPercent": 22.42,
    "dayChange": 25.50,
    "weightPercent": 35.60
  }
]
```

Holdings are cached per user and refreshed on any transaction change.

---

## Portfolio

### GET /api/portfolio/dashboard
Get portfolio dashboard summary.

**Response 200 OK**
```json
{
  "totalValue": 320000.00,
  "totalInvested": 280000.00,
  "totalGainLoss": 40000.00,
  "overallReturnPercent": 14.29,
  "xirr": 18.45,
  "totalStocks": 7,
  "dayGainLoss": 1250.00,
  "topGainer": { "symbol": "SBIN", "returnPercent": 24.5, ... },
  "topLoser":  { "symbol": "WIPRO", "returnPercent": -8.2, ... },
  "largestHolding": { "symbol": "TCS", "totalValue": 118500.00, ... }
}
```

---

### GET /api/portfolio/allocation
Get sector-wise portfolio allocation.

**Response 200 OK**
```json
[
  {
    "sector": "Information Technology",
    "totalValue": 180000.00,
    "percentage": 56.25,
    "stockCount": 4
  },
  {
    "sector": "Banking",
    "totalValue": 140000.00,
    "percentage": 43.75,
    "stockCount": 3
  }
]
```

---

### GET /api/portfolio/growth
Historical portfolio value chart (last 13 data points).

**Response 200 OK**
```json
[
  {
    "date": "2024-05-01",
    "totalValue": 295000.00,
    "totalInvested": 270000.00,
    "pnl": 25000.00,
    "monthLabel": "May '24"
  }
]
```

Data sourced from daily snapshots (if available) or fallback computation from transactions.

---

## Watchlist

### GET /api/watchlist
Get all watchlist items with current prices.

**Response 200 OK**
```json
[
  {
    "id": 5,
    "stockSymbol": "BAJFINANCE",
    "stockName": "Bajaj Finance Ltd",
    "currentPrice": 7200.00,
    "addedAt": "2024-01-10T09:00:00"
  }
]
```

---

### POST /api/watchlist
Add a symbol to the watchlist.

**Request**
```json
{
  "stockSymbol": "BAJFINANCE",
  "stockName": "Bajaj Finance Ltd"
}
```

**Response 201 Created** — WatchlistItemDTO

**Errors:** `409` already in watchlist

---

### DELETE /api/watchlist/{id}
Remove a symbol from the watchlist.

**Response 204 No Content**

---

## Price Alerts

### GET /api/alerts
Get all alerts for the user.

**Response 200 OK**
```json
[
  {
    "id": 3,
    "stockSymbol": "TCS",
    "targetPrice": 4000.00,
    "direction": "ABOVE",
    "active": true,
    "triggeredAt": null,
    "currentPrice": 3950.00,
    "createdAt": "2024-01-20T14:00:00"
  }
]
```

`direction` values: `ABOVE` | `BELOW`

---

### POST /api/alerts
Create a new price alert.

**Request**
```json
{
  "stockSymbol": "TCS",
  "targetPrice": 4000.00,
  "direction": "ABOVE"
}
```

**Response 201 Created** — PriceAlertDTO

---

### DELETE /api/alerts/{id}
Delete a price alert.

**Response 204 No Content**

---

## Analytics

### GET /api/analytics/monthly-pl
Monthly P&L breakdown for the last 12 months.

**Response 200 OK**
```json
[
  {
    "month": "2024-05",
    "monthLabel": "May '24",
    "year": 2024,
    "monthNumber": 5,
    "realisedPL": 12500.00,
    "unrealisedPL": 8200.00,
    "invested": 50000.00,
    "proceeds": 62500.00
  }
]
```

---

### GET /api/analytics/sector
Sector allocation (delegates to `/api/portfolio/allocation`).

---

### GET /api/analytics/tax-summary
STCG/LTCG tax analysis for the given financial year.

**Query Params**
| Param | Type | Default | Description |
|---|---|---|---|
| `fy` | string | current FY | Financial year, e.g. `2024-25` |

**Response 200 OK**
```json
{
  "financialYear": "2024-25",
  "stcgGain": 15000.00,
  "ltcgGain": 42000.00,
  "stcgTaxLiability": 3000.00,
  "ltcgTaxableAmount": 0.00,
  "ltcgTaxLiability": 0.00,
  "totalTaxLiability": 3000.00,
  "ltcgExemptionUsed": 42000.00,
  "ltcgExemptionLimit": 125000.00,
  "taxLines": [
    {
      "symbol": "TCS",
      "buyDate": "2022-06-01",
      "sellDate": "2024-01-15",
      "quantity": 10,
      "buyPrice": 3000.00,
      "sellPrice": 4200.00,
      "gainLoss": 12000.00,
      "taxType": "LTCG",
      "taxLiability": 0.00
    }
  ]
}
```

Tax rates (Budget 2024): STCG = 20%, LTCG = 12.5% above ₹1,25,000 exemption.

---

## Reports (CSV Downloads)

### GET /api/reports/portfolio-summary
Download current holdings as CSV.

**Response 200 OK**
- Content-Type: `text/csv`
- Content-Disposition: `attachment; filename="portfolio-summary.csv"`

Columns: Symbol, Name, Sector, Quantity, Avg Buy Price, Invested Amount, Current Price, Current Value, Gain/Loss, Return %

---

### GET /api/reports/transactions
Download transaction history as CSV.

**Query Params:** `symbol` (optional filter)

**Response 200 OK** — CSV attachment

Columns: ID, Symbol, Name, Type, Quantity, Price, Date, Brokerage, Total Amount, Notes

---

### GET /api/reports/tax-report
Download STCG/LTCG tax report as CSV.

**Query Params:** `fy` (default: current FY)

**Response 200 OK** — CSV attachment

---

## User Profile

### GET /api/user/profile
Get the authenticated user's profile.

**Response 200 OK**
```json
{
  "id": 1,
  "name": "Arjun Sharma",
  "email": "arjun@example.com",
  "currency": "INR",
  "darkMode": false,
  "totalTransactions": 18,
  "memberSince": "2024-01-10T08:00:00"
}
```

---

### PUT /api/user/profile
Update the user's profile. All fields optional.

**Request**
```json
{
  "name": "Arjun Kumar Sharma",
  "currency": "INR",
  "darkMode": true,
  "currentPassword": "oldPass123",
  "newPassword": "newSecurePass456"
}
```

**Response 200 OK** — updated UserProfileDTO

**Errors:** `409` email conflict, `400` validation

---

## Error Response Format

All errors return:
```json
{
  "timestamp": "2024-06-01T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Transaction not found with id: 99",
  "path": "/api/transactions/99",
  "fieldErrors": []
}
```

For validation errors (400), `fieldErrors` contains:
```json
[
  { "field": "quantity", "message": "must be greater than 0", "rejectedValue": 0 }
]
```
