import java.util.HashMap;
import java.util.Map;

public class Portfolio {
    private double cashBalance;
    private final Map<String, Integer> sharesOwned;
    private final Map<String, Double> costBasisMap;

    public Portfolio(double startingCapital) {
        this.cashBalance = startingCapital;
        this.sharesOwned = new HashMap<>();
        this.costBasisMap = new HashMap<>();
    }

    public double getCashBalance() { return cashBalance; }
    public void setCashBalance(double cashBalance) { this.cashBalance = cashBalance; }

    public Map<String, Integer> getSharesOwned() { return sharesOwned; }
    public Map<String, Double> getCostBasisMap() { return costBasisMap; }

    public int getSharesOf(String ticker) {
        return sharesOwned.getOrDefault(ticker, 0);
    }

    public double getCostBasisOf(String ticker) {
        return costBasisMap.getOrDefault(ticker, 0.0);
    }

    public void updatePosition(String ticker, int finalShares, double finalCostBasis) {
        if (finalShares <= 0) {
            sharesOwned.remove(ticker);
            costBasisMap.remove(ticker);
        } else {
            sharesOwned.put(ticker, finalShares);
            costBasisMap.put(ticker, finalCostBasis);
        }
    }

    public double calculateNetAssetValue(Map<String, Stock> marketData) {
        double totalValue = cashBalance;
        for (Map.Entry<String, Integer> entry : sharesOwned.entrySet()) {
            Stock s = marketData.get(entry.getKey());
            if (s != null) {
                totalValue += (s.getCurrentPrice() * entry.getValue());
            }
        }
        return totalValue;
    }

    public void clear() {
        sharesOwned.clear();
        costBasisMap.clear();
    }
}