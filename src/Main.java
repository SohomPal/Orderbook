import API.OrderBookAPI;
import lib.Order;

public class Main {
    public static void main(String[] args) {

        OrderBookAPI API = new OrderBookAPI();
        API.placeOrder("seller1", "TSLA", 700.0, 100, Order.Side.SELL, OrderBookAPI.Type.GoodTilCancel);
        API.placeOrder("seller2", "TSLA", 702.0, 300, Order.Side.SELL, OrderBookAPI.Type.FillOrKill);
        API.placeOrder("seller3", "TSLA", 698.0, 700, Order.Side.SELL, OrderBookAPI.Type.GoodTilCancel);
        API.placeOrder("seller4", "TSLA", 700.0, 300, Order.Side.SELL, OrderBookAPI.Type.GoodTilCancel);
        API.placeOrder("buyer", "TSLA", 695, 1000, Order.Side.BUY, OrderBookAPI.Type.GoodTilCancel);
        API.placeOrder("BUYER", "TSLA", 700.0, 250, Order.Side.BUY, OrderBookAPI.Type.FillOrKill);
        API.printOrderBook();
        API.placeOrder("BUYER9", "TSLA", 700.0, 250, Order.Side.BUY, OrderBookAPI.Type.FillOrKill);
        API.printOrderBook();
        API.placeOrder("BUYER10", "TSLA", 700.0, 250, Order.Side.BUY, OrderBookAPI.Type.GoodTilCancel);
        API.printOrderBook();

    }
}