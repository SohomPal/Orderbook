package lib;

import java.util.HashMap;
import java.util.Map;

public class Orderbook {

    private final Map<String, StockOrders> bids;
    private final Map<String, StockOrders> asks;

    public Orderbook() {
        this.bids = new HashMap<>();
        this.asks = new HashMap<>();
    }

    // Add a new order to the appropriate side
    public void addOrder(Order order) {
        String symbol = order.getStockSymbol();
        StockOrders.Side side = order.getSide() == Order.Side.BUY ? StockOrders.Side.BUY : StockOrders.Side.SELL;
        Map<String, StockOrders> bookSide = side == StockOrders.Side.BUY ? bids : asks;

        bookSide.computeIfAbsent(symbol, s -> new StockOrders(side)).addOrder(order);
    }

    // Get best bid price for a stock symbol
    public Double getBestBid(String symbol) {
        StockOrders stockBids = bids.get(symbol);
        return stockBids != null ? stockBids.getBestPrice() : null;
    }

    // Get best ask price for a stock symbol
    public Double getBestAsk(String symbol) {
        StockOrders stockAsks = asks.get(symbol);
        return stockAsks != null ? stockAsks.getBestPrice() : null;
    }

    // Get all orders by side for a stock
    public StockOrders getOrdersBySide(String symbol, StockOrders.Side side) {
        return side == StockOrders.Side.BUY ? bids.get(symbol) : asks.get(symbol);
    }

    // Remove a specific order by object
    public boolean removeOrder(Order order) {
        String symbol = order.getStockSymbol();
        Map<String, StockOrders> bookSide = order.getSide() == Order.Side.BUY ? bids : asks;

        StockOrders stockOrders = bookSide.get(symbol);
        return stockOrders != null && stockOrders.removeOrder(order);
    }
}
