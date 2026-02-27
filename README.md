# FinTech Portfolio

```
  ¦¦¦¦¦¦+ ¦¦¦¦¦¦+ ¦¦¦+   ¦¦¦+¦¦+¦¦¦+   ¦¦+ ¦¦¦¦¦¦+      ¦¦¦¦¦¦¦+ ¦¦¦¦¦¦+  ¦¦¦¦¦¦+ ¦¦¦+   ¦¦+
 ¦¦+----+¦¦+---¦¦+¦¦¦¦+ ¦¦¦¦¦¦¦¦¦¦¦¦+  ¦¦¦¦¦+----+      ¦¦+----+¦¦+---¦¦+¦¦+---¦¦+¦¦¦¦+  ¦¦¦
 ¦¦¦     ¦¦¦   ¦¦¦¦¦+¦¦¦¦+¦¦¦¦¦¦¦¦+¦¦+ ¦¦¦¦¦¦  ¦¦¦+     ¦¦¦¦¦¦¦+¦¦¦   ¦¦¦¦¦¦   ¦¦¦¦¦+¦¦+ ¦¦¦
 ¦¦¦     ¦¦¦   ¦¦¦¦¦¦+¦¦++¦¦¦¦¦¦¦¦¦+¦¦+¦¦¦¦¦¦   ¦¦¦     +----¦¦¦¦¦¦   ¦¦¦¦¦¦   ¦¦¦¦¦¦+¦¦+¦¦¦
 +¦¦¦¦¦¦++¦¦¦¦¦¦++¦¦¦ +-+ ¦¦¦¦¦¦¦¦¦ +¦¦¦¦¦+¦¦¦¦¦¦++     ¦¦¦¦¦¦¦¦+¦¦¦¦¦¦+++¦¦¦¦¦¦++¦¦¦ +¦¦¦¦¦
  +-----+ +-----+ +-+     +-++-++-+  +---+ +-----+      +------+ +-----+  +-----++-+  +---+
```

> A full-stack FinTech portfolio management app for tracking Indian stock investments — built with React, TypeScript & Tailwind CSS.

---

## ?? Features

### ?? Authentication
Secure login and registration with JWT-based session management and protected routes.

### ?? Dashboard
Portfolio overview at a glance — Total Invested, Current Value, P&L, XIRR, Top Gainers/Losers, Portfolio Allocation pie chart and Growth line chart.

### ?? Transactions
Full CRUD for Buy/Sell transactions. Filter by stock, add brokerage/notes, with confirmation dialogs and toast notifications.

### ?? Holdings
View all current holdings with Avg Buy Price, Current Price, P&L, Return %, Sector, Weight % and Days Held — all color coded.

### ?? Analytics
Monthly P&L bar chart and Sector Allocation pie chart. Realized/Unrealized gains and Risk meter (backend integration ready).

### ?? Reports
Export Portfolio Summary as PDF and Transaction History / Holdings as Excel — backend integration ready.

### ??? Watchlist
Add and track stocks you're eyeing with live price and % change indicators.

### ?? Price Alerts
Set target price alerts with Active/Inactive status and directional triggers.

### ?? Settings
Update profile (name/email) and toggle Dark Mode.

---

## ??? Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | React 18 + TypeScript |
| Build Tool | Vite |
| Styling | Tailwind CSS + shadcn/ui |
| Routing | React Router v6 |
| Charts | Recharts |
| State | TanStack Query + Context API |
| Testing | Vitest + Testing Library |

---

## ?? Branch Structure

| Branch | Purpose | Status |
|--------|---------|--------|
| `main` | Stable base | ? Active |
| `frontend` | React + TypeScript UI | ? Complete |
| `backend` | REST API & Server logic | ?? Planned |
| `database` | DB Schemas & Migrations | ?? Planned |

---

## ? Getting Started

```sh
# Clone the repo
git clone https://github.com/ihashetty/FinTechPortfolio.git

# Switch to frontend branch
git checkout frontend

# Install dependencies
npm install

# Start dev server
npm run dev
```

App runs at `http://localhost:8080`

> Use the pre-filled demo credentials on the login page to explore the app.
