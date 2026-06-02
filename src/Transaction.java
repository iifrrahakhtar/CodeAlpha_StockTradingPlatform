public class Transaction {
    private final String ticker;
    private final String type;
    private final int shares;
    private final double price;
    private final String timestamp;

    public Transaction(String ticker, String type, int shares, double price, String timestamp) {
        this.ticker = ticker;
        this.type = type;
        this.shares = shares;
        this.price = price;
        this.timestamp = timestamp;
    }

    public String getTicker() { return ticker; }
    public String getType() { return type; }
    public int getShares() { return shares; }
    public double getPrice() { return price; }
    public String getTimestamp() { return timestamp; }
}