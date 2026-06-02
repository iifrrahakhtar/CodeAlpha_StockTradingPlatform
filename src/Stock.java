public class Stock {
    private final String ticker;
    private final String name;
    private double currentPrice;
    private double changePercent;

    public Stock(String ticker, String name, double initialPrice) {
        this.ticker = ticker;
        this.name = name;
        this.currentPrice = initialPrice;
        this.changePercent = 0.0;
    }

    public void updatePrice(double pct) {
        this.changePercent = pct * 100;
        this.currentPrice += this.currentPrice * pct;
        if (this.currentPrice < 1.0) {
            this.currentPrice = 1.0;
        }
    }

    public String getTicker() { return ticker; }
    public String getName() { return name; }
    public double getCurrentPrice() { return currentPrice; }
    public double getChangePercent() { return changePercent; }
}