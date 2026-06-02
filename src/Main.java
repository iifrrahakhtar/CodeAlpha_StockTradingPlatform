import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.*;

public class Main extends JFrame {

    private static final String DB_URL = "jdbc:sqlite:trading_platform.db";

    // --- Core Structured Modules ---
    private final Map<String, Stock> marketData = new LinkedHashMap<>();
    private final Portfolio portfolio = new Portfolio(10000.00);

    // --- Swing UI Components ---
    private JLabel lblCash, lblPortfolioValue;
    private DefaultTableModel marketTableModel;
    private DefaultTableModel portfolioTableModel;
    private DefaultTableModel historyTableModel;
    private JTextArea txtLog;
    private JComboBox<String> cmbStocks;
    private JTextField txtShares;
    private PortfolioChart perfChart;
    private final Random random = new Random();

    // --- Palette styling ---
    private final Color COLOR_BG = new Color(20, 24, 33);
    private final Color COLOR_CARD = new Color(28, 34, 46);
    private final Color COLOR_ACCENT = new Color(0, 200, 115);
    private final Color COLOR_TEXT_MAIN = Color.WHITE;
    private final Color COLOR_TEXT_MUTED = new Color(150, 160, 175);
    private final Color COLOR_BORDER = new Color(42, 52, 71);

    public Main() {
        initDatabase();
        initializeMarket();
        loadPortfolioFromDB();

        setTitle("Apex Pro | Quantitative Trading Terminal");
        setSize(1250, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        // --- TOP TELEMETRY BAR ---
        JPanel topBar = new JPanel(new GridLayout(1, 2, 20, 0));
        topBar.setBackground(COLOR_BG);
        topBar.setBorder(new EmptyBorder(20, 25, 10, 25));

        JPanel cardCash = createMetricCard("AVAILABLE LIQUID FUNDS (CASH)", String.format("$%.2f", portfolio.getCashBalance()), COLOR_ACCENT);
        JPanel cardValue = createMetricCard("NET LIQUIDATION ASSET VALUE", String.format("$%.2f", portfolio.calculateNetAssetValue(marketData)), new Color(33, 150, 243));

        lblCash = (JLabel) cardCash.getClientProperty("valueLabel");
        lblPortfolioValue = (JLabel) cardValue.getClientProperty("valueLabel");

        topBar.add(cardCash);
        topBar.add(cardValue);
        add(topBar, BorderLayout.NORTH);

        // --- CENTRAL WORKSPACE DIVISION ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(COLOR_CARD);
        tabbedPane.setForeground(COLOR_TEXT_MAIN);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel mainWorkspace = new JPanel(new GridLayout(1, 2, 20, 0));
        mainWorkspace.setBackground(COLOR_BG);
        mainWorkspace.setBorder(new EmptyBorder(10, 25, 20, 25));

        // Left Panel: Market Feed Data Screen
        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setBackground(COLOR_BG);

        marketTableModel = new DefaultTableModel(new String[]{"Ticker", "Company", "Price", "Change"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblMarket = new JTable(marketTableModel);
        styleTable(tblMarket);
        leftPanel.add(new JLabel("LIVE MARKET MONITOR") {{ setFont(new Font("Segoe UI", Font.BOLD, 14)); setForeground(COLOR_TEXT_MAIN); }}, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(tblMarket), BorderLayout.CENTER);

        // Right Panel: Positions + Chart + Operations Dashboard
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setBackground(COLOR_BG);

        portfolioTableModel = new DefaultTableModel(new String[]{"Asset", "Qty Owned", "Avg Cost Basis", "Value Now", "Total P/L"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblPortfolio = new JTable(portfolioTableModel);
        styleTable(tblPortfolio);

        JPanel rightSplit = new JPanel(new GridLayout(2, 1, 0, 15));
        rightSplit.setBackground(COLOR_BG);

        // Subpanel top: Holdings list
        JPanel pnlHoldings = new JPanel(new BorderLayout(0, 5));
        pnlHoldings.setBackground(COLOR_BG);
        pnlHoldings.add(new JLabel("OPEN EQUITY ACCOUNT POSITIONS") {{ setFont(new Font("Segoe UI", Font.BOLD, 14)); setForeground(COLOR_TEXT_MAIN); }}, BorderLayout.NORTH);
        pnlHoldings.add(new JScrollPane(tblPortfolio), BorderLayout.CENTER);
        rightSplit.add(pnlHoldings);

        // Operational Panel Assembly (Input controls + Mini Chart Graphic)
        JPanel pnlActions = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlActions.setBackground(COLOR_BG);

        JPanel pnlOrderForm = new JPanel();
        pnlOrderForm.setBackground(COLOR_CARD);
        pnlOrderForm.setBorder(BorderFactory.createCompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(15, 15, 15, 15)));
        pnlOrderForm.setLayout(new BoxLayout(pnlOrderForm, BoxLayout.Y_AXIS));

        JLabel lblFormTitle = new JLabel("EXECUTE MARKET ORDER");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFormTitle.setForeground(COLOR_TEXT_MAIN);

        cmbStocks = new JComboBox<>(marketData.keySet().toArray(new String[0]));
        styleComboBox(cmbStocks);

        txtShares = new JTextField();
        styleTextField(txtShares, "Quantity (e.g. 10)");

        JButton btnBuy = new JButton("TRANSACT BUY / LONG");
        styleButton(btnBuy, COLOR_ACCENT);
        btnBuy.addActionListener(e -> executeTrade(true));

        JButton btnSell = new JButton("TRANSACT SELL / SHORT");
        styleButton(btnSell, new Color(239, 83, 80));
        btnSell.addActionListener(e -> executeTrade(false));

        pnlOrderForm.add(lblFormTitle); pnlOrderForm.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlOrderForm.add(cmbStocks); pnlOrderForm.add(Box.createRigidArea(new Dimension(0, 8)));
        pnlOrderForm.add(txtShares); pnlOrderForm.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlOrderForm.add(btnBuy); pnlOrderForm.add(Box.createRigidArea(new Dimension(0, 6)));
        pnlOrderForm.add(btnSell);
        pnlActions.add(pnlOrderForm);

        // Middle Right: Equity Analytics Vector Visualizer Graph Panel
        JPanel pnlChartContainer = new JPanel(new BorderLayout(0, 5));
        pnlChartContainer.setBackground(COLOR_BG);
        pnlChartContainer.add(new JLabel("REAL-TIME PORTFOLIO NET WORTH MONITOR") {{ setFont(new Font("Segoe UI", Font.BOLD, 11)); setForeground(COLOR_TEXT_MUTED); }}, BorderLayout.NORTH);
        perfChart = new PortfolioChart();
        perfChart.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        pnlChartContainer.add(perfChart, BorderLayout.CENTER);
        pnlActions.add(pnlChartContainer);

        rightSplit.add(pnlActions);
        rightPanel.add(rightSplit, BorderLayout.CENTER);

        mainWorkspace.add(leftPanel);
        mainWorkspace.add(rightPanel);
        tabbedPane.addTab("Trading Dashboard", mainWorkspace);

        // --- SECONDARY VIEW: SYSTEM HISTORY AUDIT LEDGER ---
        JPanel historyWorkspace = new JPanel(new BorderLayout(0, 15));
        historyWorkspace.setBackground(COLOR_BG);
        historyWorkspace.setBorder(new EmptyBorder(20, 25, 20, 25));

        historyTableModel = new DefaultTableModel(new String[]{"Timestamp", "Ticker Asset", "Action Type", "Shares Delta", "Execution Price"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblHistory = new JTable(historyTableModel);
        styleTable(tblHistory);
        historyWorkspace.add(new JLabel("HISTORICAL AUDIT TRANSACTION LEDGER") {{ setFont(new Font("Segoe UI", Font.BOLD, 14)); setForeground(COLOR_TEXT_MAIN); }}, BorderLayout.NORTH);
        historyWorkspace.add(new JScrollPane(tblHistory), BorderLayout.CENTER);

        tabbedPane.addTab("Transaction Audit Ledger", historyWorkspace);

        // --- BOTTOM LIVE SYSTEM LOGGER ---
        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setBackground(COLOR_BG);
        bottomWrapper.setBorder(new EmptyBorder(0, 25, 15, 25));
        txtLog = new JTextArea("[SYSTEM LOG] Trading terminal online. Managed entity maps loaded smoothly.\n");
        txtLog.setBackground(COLOR_CARD);
        txtLog.setForeground(new Color(170, 255, 170));
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtLog.setEditable(false);
        txtLog.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane logScroll = new JScrollPane(txtLog);
        logScroll.setPreferredSize(new Dimension(100, 100));
        logScroll.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        bottomWrapper.add(logScroll, BorderLayout.CENTER);

        add(tabbedPane, BorderLayout.CENTER);
        add(bottomWrapper, BorderLayout.SOUTH);

        updateUIDisplays();
        loadHistoryFromDB();
        startMarketSimulation();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS portfolio (id INTEGER PRIMARY KEY CHECK (id = 1), cash_balance REAL);");
            stmt.execute("INSERT OR IGNORE INTO portfolio(id, cash_balance) VALUES(1, 10000.00);");
            stmt.execute("CREATE TABLE IF NOT EXISTS positions (ticker TEXT PRIMARY KEY, shares INTEGER, cost_basis REAL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, ticker TEXT, type TEXT, shares INTEGER, price REAL, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Engine initiation error: " + e.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPortfolioFromDB() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            ResultSet rsCash = stmt.executeQuery("SELECT cash_balance FROM portfolio WHERE id = 1;");
            if (rsCash.next()) {
                portfolio.setCashBalance(rsCash.getDouble("cash_balance"));
            }

            ResultSet rsPositions = stmt.executeQuery("SELECT * FROM positions;");
            portfolio.clear();
            while (rsPositions.next()) {
                String ticker = rsPositions.getString("ticker");
                int shares = rsPositions.getInt("shares");
                double cost = rsPositions.getDouble("cost_basis");
                portfolio.updatePosition(ticker, shares, cost);
            }
        } catch (SQLException e) {
            logAction("[DB ERROR] Failed reading profile states.");
        }
    }

    private void loadHistoryFromDB() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM transactions ORDER BY timestamp DESC;")) {

            historyTableModel.setRowCount(0);
            while (rs.next()) {
                historyTableModel.addRow(new Object[]{
                        rs.getString("timestamp"),
                        rs.getString("ticker"),
                        rs.getString("type"),
                        rs.getInt("shares"),
                        String.format("$%.2f", rs.getDouble("price"))
                });
            }
        } catch (SQLException e) {
            logAction("[DB ERROR] Failed fetching ledger trail records.");
        }
    }

    private void saveTradeToDatabase(Transaction tx, double newCash) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try (PreparedStatement psCash = conn.prepareStatement("UPDATE portfolio SET cash_balance = ? WHERE id = 1;")) {
                psCash.setDouble(1, newCash);
                psCash.executeUpdate();
            }

            int currentShares = portfolio.getSharesOf(tx.getTicker());
            if (currentShares == 0) {
                try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM positions WHERE ticker = ?;")) {
                    psDel.setString(1, tx.getTicker());
                    psDel.executeUpdate();
                }
            } else {
                try (PreparedStatement psUpsert = conn.prepareStatement(
                        "INSERT INTO positions(ticker, shares, cost_basis) VALUES(?, ?, ?) ON CONFLICT(ticker) DO UPDATE SET shares=excluded.shares, cost_basis=excluded.cost_basis;")) {
                    psUpsert.setString(1, tx.getTicker());
                    psUpsert.setInt(2, currentShares);
                    psUpsert.setDouble(3, portfolio.getCostBasisOf(tx.getTicker()));
                    psUpsert.executeUpdate();
                }
            }

            try (PreparedStatement psLog = conn.prepareStatement("INSERT INTO transactions(ticker, type, shares, price) VALUES(?, ?, ?, ?);")) {
                psLog.setString(1, tx.getTicker());
                psLog.setString(2, tx.getType());
                psLog.setInt(3, tx.getShares());
                psLog.setDouble(4, tx.getPrice());
                psLog.executeUpdate();
            }

            conn.commit();
            loadHistoryFromDB(); // Sync database view tab changes instantly
        } catch (SQLException e) {
            logAction("[DB CRITICAL] Transaction Rollback triggered: " + e.getMessage());
        }
    }

    private void executeTrade(boolean isBuy) {
        String ticker = (String) cmbStocks.getSelectedItem();
        String inputQty = txtShares.getText().trim();

        if (inputQty.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Order Rejected: Transaction shares count field is empty.", "Execution Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int shares = Integer.parseInt(inputQty);
            if (shares <= 0) {
                JOptionPane.showMessageDialog(this, "Order Rejected: Shares quantity metric must be greater than zero.", "Execution Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Stock stock = marketData.get(ticker);
            double orderValue = stock.getCurrentPrice() * shares;
            double updatedCash = portfolio.getCashBalance();

            if (isBuy) {
                if (orderValue > updatedCash) {
                    // Fixed Bug: JComponent.WHEN_FOCUSED parameter fixed with standard JOptionPane message type enum
                    JOptionPane.showMessageDialog(this, "Liquidity Crunch: Insufficient cash balance reserves to fulfill buy order.", "Execution Denied", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                updatedCash -= orderValue;
                int existingQty = portfolio.getSharesOf(ticker);
                double currentCostBasis = portfolio.getCostBasisOf(ticker);
                double newCostBasis = ((currentCostBasis * existingQty) + orderValue) / (existingQty + shares);

                portfolio.setCashBalance(updatedCash);
                portfolio.updatePosition(ticker, existingQty + shares, newCostBasis);

                Transaction tx = new Transaction(ticker, "BUY", shares, stock.getCurrentPrice(), "NOW");
                logAction(String.format("EXECUTION [BUY]: %d shares of %s at $%.2f", shares, ticker, stock.getCurrentPrice()));
                saveTradeToDatabase(tx, updatedCash);
            } else {
                int existingQty = portfolio.getSharesOf(ticker);
                if (shares > existingQty) {
                    JOptionPane.showMessageDialog(this, "Short-Circuit Rejection: You cannot liquidate more shares than you hold.", "Execution Denied", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                updatedCash += orderValue;
                portfolio.setCashBalance(updatedCash);
                portfolio.updatePosition(ticker, existingQty - shares, portfolio.getCostBasisOf(ticker));

                Transaction tx = new Transaction(ticker, "SELL", shares, stock.getCurrentPrice(), "NOW");
                logAction(String.format("EXECUTION [SELL]: %d shares of %s at $%.2f", shares, ticker, stock.getCurrentPrice()));
                saveTradeToDatabase(tx, updatedCash);
            }

            txtShares.setText("");
            updateUIDisplays();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Entry Detected: Numeric inputs only.", "Parsing Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startMarketSimulation() {
        javax.swing.Timer timer = new javax.swing.Timer(3000, e -> {
            for (Stock stock : marketData.values()) {
                double changePercent = (random.nextDouble() * 4.0 - 2.0) / 100.0;
                stock.updatePrice(changePercent);
            }
            updateUIDisplays();
        });
        timer.start();
    }

    private void updateUIDisplays() {
        double totalNAV = portfolio.calculateNetAssetValue(marketData);
        lblCash.setText(String.format("$%.2f", portfolio.getCashBalance()));
        lblPortfolioValue.setText(String.format("$%.2f", totalNAV));

        // Push current balance into historical vector line engine
        perfChart.addValue(totalNAV);

        marketTableModel.setRowCount(0);
        for (Stock s : marketData.values()) {
            String trackingTrendStr = String.format("%s %.2f%%", (s.getChangePercent() >= 0 ? "▲" : "▼"), Math.abs(s.getChangePercent()));
            marketTableModel.addRow(new Object[]{s.getTicker(), s.getName(), String.format("$%.2f", s.getCurrentPrice()), trackingTrendStr});
        }

        portfolioTableModel.setRowCount(0);
        for (Map.Entry<String, Integer> entry : portfolio.getSharesOwned().entrySet()) {
            String ticker = entry.getKey();
            int qty = entry.getValue();
            double costBasis = portfolio.getCostBasisOf(ticker);
            double currentPrice = marketData.get(ticker).getCurrentPrice();

            double valueNow = currentPrice * qty;
            double netPL = valueNow - (costBasis * qty);

            portfolioTableModel.addRow(new Object[]{
                    ticker, qty, String.format("$%.2f", costBasis), String.format("$%.2f", valueNow), String.format("$%.2f", netPL)
            });
        }
    }

    private void initializeMarket() {
        marketData.put("AAPL", new Stock("AAPL", "Apple Inc.", 175.50));
        marketData.put("TSLA", new Stock("TSLA", "Tesla Motors", 210.20));
        marketData.put("NVDA", new Stock("NVDA", "NVIDIA Corp.", 875.00));
        marketData.put("MSFT", new Stock("MSFT", "Microsoft Corp.", 415.30));
        marketData.put("AMZN", new Stock("AMZN", "Amazon.com Inc.", 178.10));
    }

    private void logAction(String text) {
        txtLog.append(String.format("[%tT] %s\n", new java.util.Date(), text));
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    private JPanel createMetricCard(String title, String val, Color highlight) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); g.setColor(highlight); g.fillRect(0, 0, 5, getHeight());
            }
        };
        card.setBackground(COLOR_CARD);
        card.setLayout(new GridLayout(2, 1, 0, 4));
        card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(14, 18, 14, 18)));

        JLabel lblTitle = new JLabel(title) {{ setFont(new Font("Segoe UI", Font.BOLD, 11)); setForeground(COLOR_TEXT_MUTED); }};
        JLabel lblVal = new JLabel(val) {{ setFont(new Font("Segoe UI", Font.BOLD, 22)); setForeground(COLOR_TEXT_MAIN); }};

        card.add(lblTitle); card.add(lblVal);
        card.putClientProperty("valueLabel", lblVal);
        return card;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setBackground(COLOR_CARD);
        table.setForeground(COLOR_TEXT_MAIN);
        table.setGridColor(COLOR_BORDER);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(42, 52, 71));
        table.getTableHeader().setBackground(COLOR_BORDER);
        table.getTableHeader().setForeground(COLOR_TEXT_MAIN);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setBorder(BorderFactory.createEmptyBorder());

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 1) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void styleComboBox(JComboBox<String> box) {
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        box.setPreferredSize(new Dimension(box.getPreferredSize().width, 40));
        box.setBackground(COLOR_BG);
        box.setForeground(COLOR_TEXT_MAIN);
        box.setFont(new Font("Segoe UI", Font.BOLD, 13));
        box.setBorder(new LineBorder(COLOR_BORDER, 1));
    }

    private void styleTextField(JTextField field, String placeholder) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 40));
        field.setBackground(COLOR_BG);
        field.setForeground(COLOR_TEXT_MAIN);
        field.setCaretColor(COLOR_ACCENT);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(0, 10, 0, 10)));
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 42));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}