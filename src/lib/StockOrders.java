package lib;

import java.util.*;

public class StockOrders {

    public enum Side {
        BUY, SELL
    }

    private final Side side;
    private long totalVolume;
    private final PriorityQueue<FixedPriceOrderQueue> priceHeap;
    private double bestPrice;
    private final Map<Double, FixedPriceOrderQueue> priceToQueueMap;

    public StockOrders(Side side) {
        this.side = side;
        this.totalVolume = 0;
        this.priceToQueueMap = new HashMap<>();

        Comparator<FixedPriceOrderQueue> comparator = (a, b) -> {
            return side == Side.BUY
                    ? Double.compare(b.getPrice(), a.getPrice())
                    : Double.compare(a.getPrice(), b.getPrice());
        };

        this.priceHeap = new PriorityQueue<>(comparator);
        this.bestPrice = -1;
    }

    public Side getSide() {
        return side;
    }

    public long getTotalVolume() {
        return totalVolume;
    }

    public double getBestPrice() {
        return bestPrice;
    }

    public Map<Double, FixedPriceOrderQueue> getPriceToQueueMap() {
        return Collections.unmodifiableMap(priceToQueueMap);
    }

    public void addOrder(Order order) {
        double price = order.getPrice();
        FixedPriceOrderQueue queue = priceToQueueMap.get(price);

        if (queue == null) {
            queue = new FixedPriceOrderQueue(price);
            priceToQueueMap.put(price, queue);
            priceHeap.offer(queue);
        }

        queue.addOrder(order);
        totalVolume += order.getVolume();
        updateBestPrice();
    }

    // Returns the FixedPriceOrderQueue with best price
    public FixedPriceOrderQueue peekTopQueue() {
        cleanupHeap();
        return priceHeap.peek();
    }

    // Removes and returns the top order from best price level
    public Order pollTopOrder() {
        cleanupHeap();
        FixedPriceOrderQueue topQueue = priceHeap.peek();
        if (topQueue == null) return null;

        Order topOrder = topQueue.pollOrder();
        if (topOrder != null) {
            totalVolume -= topOrder.getVolume();
            if (topQueue.getTotalVolume() == 0) {
                priceToQueueMap.remove(topQueue.getPrice());
                priceHeap.poll(); // remove empty queue
            }
        }

        updateBestPrice();
        return topOrder;
    }

    // Reduces volume at a specific price level
    public void reduceVolumeAtPrice(double price, long delta) {
        FixedPriceOrderQueue queue = priceToQueueMap.get(price);
        if (queue != null) {
            queue.adjustTotalVolume(-delta);
            totalVolume -= delta;
            if (queue.getTotalVolume() <= 0) {
                priceToQueueMap.remove(price);
                cleanupHeap();
            }
            updateBestPrice();
        }
    }

    // Removes a specific order from the order book (linear scan within price level)
    public boolean removeOrder(Order order) {
        FixedPriceOrderQueue queue = priceToQueueMap.get(order.getPrice());
        if (queue == null) return false;

        Queue<Order> oldQueue = queue.getOrders();
        Queue<Order> newQueue = new LinkedList<>();

        boolean removed = false;
        while (!oldQueue.isEmpty()) {
            Order o = oldQueue.poll();
            if (!removed && o.getOrderId().equals(order.getOrderId())) {
                totalVolume -= o.getVolume();
                queue.adjustTotalVolume(-o.getVolume());
                removed = true;
            } else {
                newQueue.add(o);
            }
        }

        if (removed) {
            queue.getOrders().clear();
            newQueue.forEach(queue::addOrder);
        }

        if (queue.getTotalVolume() <= 0) {
            priceToQueueMap.remove(order.getPrice());
            cleanupHeap();
        }

        updateBestPrice();
        return removed;
    }

    // Internal: Removes empty queues from heap top
    private void cleanupHeap() {
        while (!priceHeap.isEmpty() && priceHeap.peek().getTotalVolume() <= 0) {
            FixedPriceOrderQueue empty = priceHeap.poll();
            priceToQueueMap.remove(empty.getPrice());
        }
    }

    // Recompute best price after add/remove
    private void updateBestPrice() {
        cleanupHeap();
        bestPrice = priceHeap.isEmpty() ? -1 : priceHeap.peek().getPrice();
    }
}
