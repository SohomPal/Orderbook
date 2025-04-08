package API;

import lib.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class OrderBookAPI {
    private final OrderBook orderBook;

    public enum Type {
        FillOrKill, GoodTilCancel
    }


        public OrderBookAPI() {
            this.orderBook = new OrderBook();
        }

        // 1. Place a new order
        public String placeOrder(String userId, String stockSymbol, double price, long volume, Order.Side side, Type orderType) {
            // TODO: Validate inputs, check business logic

            // Check if stock to cross order with is available

            // We want to cross the order with stock from the opposing side
            Order.Side SIDE = side == Order.Side.BUY ? Order.Side.SELL : Order.Side.BUY;

            if (!orderBook.getStockAvailability(stockSymbol, SIDE)){
                if(orderType == Type.FillOrKill){
                    // Order cannot be executed or added to order book
                    return null;
                }
            }

            // Check cross-able stock volume
            if((orderBook.getTotalStockVolume(stockSymbol, SIDE) < volume ||
                    orderBook.getCrossableVolume(stockSymbol, SIDE, price) < volume)
                    && orderType == Type.FillOrKill){
                // Order cannot be executed or added to order book
                return null;
            }

            // The order can be either fully or partially executed
            Order newOrder = new Order.Builder(userId)
                    .stockSymbol(stockSymbol)
                    .price(price)
                    .volume(volume)
                    .side(side)
                    .build();

            // Attempt to execute the order before adding to book as long as there are orders to cross with
            // Attempt to execute the order before adding to book as long as there are orders to cross with
            if(!executeOrder(newOrder, SIDE)) {
                orderBook.addOrder(newOrder);
                return newOrder.getOrderId();
            }
            return "Order Fulfilled";

        }

        private boolean executeOrder(Order order, Order.Side SIDE){
            long desiredVolume = order.getVolume();

            StockOrders orders = orderBook.getOrdersBySide(order.getStockSymbol(), SIDE);

            // Attempt to execute order to completion
            long crossableVolume = orderBook.getCrossableVolume(order.getStockSymbol(), SIDE, order.getPrice());
            while(desiredVolume > 0 && crossableVolume > 0){

                // Poll the best offer from the book to begin order execution
                Order topOrder = orders.pollTopOrder();

                if(topOrder.getVolume() > desiredVolume){
                    // Executes order and updates partially fulfilled order in order-book, accordingly updates volume
                    topOrder.adjustVolume(-desiredVolume);
                    orders.addOrder(topOrder);
                    desiredVolume = 0;
                }
                else{
                    // Executes order, removes order from book, and accordingly updates volume
                    desiredVolume -= topOrder.getVolume();
                    crossableVolume -= topOrder.getVolume();
                }

                // Log each executed order
            }

            // If order still has volume remaining adjust the order before returning
            if(desiredVolume > 0) {
                order.adjustVolume((-order.getVolume()) + desiredVolume);
                return false;
            }
            return true;
        }

        // 2. Cancel an existing order
        public boolean cancelOrder(String orderId) {
            Order toBeDeleted = orderBook.getOrder(orderId);
            return orderBook.removeOrder(toBeDeleted);
        }

        // 3. Get the best buy or sell price of a current stock
        public double quotePrice(String stockSymbol, Order.Side side) {
            Double price = (side == Order.Side.BUY)
                    ? orderBook.getBestBid(stockSymbol)
                    : orderBook.getBestAsk(stockSymbol);
            return price != null ? price : -1.0;
        }


    // 4. Get the volume of a stock at a specific price and side
        public long getVolumeAtPrice(String symbol, double price, Order.Side side){
            StockOrders bookside = orderBook.getOrdersBySide(symbol, side);
            if(bookside != null) {
                Map<Double, FixedPriceOrderQueue> prices = bookside.getPriceToQueueMap();
                if (prices.get(price) != null) {
                    long volume = prices.get(price).getTotalVolume();
                    return volume;
                }
            }
            return 0;
        }

        public void printOrderBook(){
            orderBook.printOrderBook();
        }
}