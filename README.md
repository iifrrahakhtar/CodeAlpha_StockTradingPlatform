# Apex Pro | Quantitative Trading Terminal

Apex Pro is an elegant, real-time desktop stock trading terminal simulator built entirely in Java. The application delivers live exchange fluctuations, algorithmic execution commands (BUY/LONG & SELL/SHORT), a permanent historical audit log synced to a local SQLite database engine, and an interactive real-time performance net-worth trend monitor.

Designed with clean Object-Oriented Programming (OOP) principles, the project decouples application state, business trading logic, analytics engine rendering, and relational storage schemas across a modular framework architecture.

---

## Key Features

* 📊 **Live Market Simulation:** Background engine updates asset evaluations every 3 seconds across high-liquidity stock symbols (`AAPL`, `NVDA`, `MSFT`, `TSLA`, `AMZN`) utilizing controlled randomized bounds.
* 📈 **Real-Time Vector Line Chart:** Dynamically recalculates and plots the account's Net Asset Value (NAV) trend windows on a customized UI graphics panel component.
* 🗃️ **Robust Database Layer:** Full local data persistence utilizing `SQLite JDBC`. State transitions, open position inventory maps, cash balance allocations, and historical ledger audit items survive application recycles.
* 🛡️ **Production-Ready OOP Modular Design:** Decoupled structural models managing distinct logic blocks across explicit object definitions (`Stock`, `Transaction`, `Portfolio`, `PortfolioChart`).
* 🎨 **Minimalist UI Styling:** Hand-crafted, modern dark-mode terminal layout featuring structured tab partitions, telemetry KPI metrics status bars, and responsive text logging windows.

---

## Domain Architecture & OOP Structure

The application's logic architecture is split cleanly into explicit decoupled structural components:

* **`Main.java`**: The core operational controller orchestrating Swing window UI construction, SQL state syncing, data state-refresh pipelines, and thread timers.
* **`Portfolio.java`**: Object encapsulation layer tracking capital metrics, asset maps, and algorithmic cost-basis formulas.
* **`Stock.java`**: Immutable attribute model defining data symbols, company profiling, and mathematical value shift vectors.
* **`Transaction.java`**: Ledger tracking data class defining individual delta-execution characteristics for audit records.
* **`PortfolioChart.java`**: Custom pixel-drawn component rendering coordinate vectors tracking financial net wealth over runtime intervals.

---

## System Requirements & Prerequisites

* **Java Development Kit (JDK):** Version 8 or higher (JDK 17+ recommended).
* **Database Library Drivers:** `sqlite-jdbc` and `slf4j-api` dependencies integrated into the IDE project configuration path.

---

## Getting Started / Execution

1. Clone this repository to your local system environment:
   ```bash
   git clone [https://github.com/YOUR_USERNAME/ApexPro-Trading-Terminal.git](https://github.com/YOUR_USERNAME/ApexPro-Trading-Terminal.git)
