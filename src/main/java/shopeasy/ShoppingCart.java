package shopeasy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A mutable shopping cart that holds {@link CartItem}s.
 *
 * <p>Contracts are enforced at runtime via {@code assert} on
 * {@link #addItem(Product, int)} and {@link #applyDiscount(double)} when the
 * JVM is started with {@code -ea}. See the Javadoc on each method for the
 * pre- and post-conditions.
 *
 * <p><strong>Invariant:</strong> {@link #total()} is always &gt;= 0 after any operation.
 */
public class ShoppingCart {

    private final List<CartItem> items = new ArrayList<>();

    /**
     * Adds a product to the cart. If the product is already present, the quantities
     * are combined into the existing cart line.
     *
     * <p><em>Pre-condition:</em> {@code product != null}, {@code quantity > 0}<br>
     * <em>Post-condition:</em> the cart contains an entry for {@code product} and the
     * number of distinct lines does not shrink.
     *
     * @param product  the product to add (must not be null)
     * @param quantity number of units to add (must be > 0)
     */
    public void addItem(Product product, int quantity) {
        // pre-conditions
        assert product != null : "addItem pre: product must not be null";
        assert quantity > 0   : "addItem pre: quantity must be > 0";

        int countBefore = items.size();

        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                // post: same number of lines (we merged into an existing one)
                assert items.size() == countBefore : "addItem post: line count must not change on merge";
                assert total() >= 0 : "invariant: total >= 0";
                return;
            }
        }
        items.add(new CartItem(product, quantity));
        // post: exactly one new line was added
        assert items.size() == countBefore + 1 : "addItem post: a new line must have been added";
        assert total() >= 0 : "invariant: total >= 0";
    }

    /**
     * Removes all units of the given product from the cart.
     * If the product is not in the cart, this method does nothing.
     *
     * @param productId the id of the product to remove
     */
    public void removeItem(String productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    /**
     * Updates the quantity of an existing cart line.
     *
     * @param productId the id of the product whose quantity should change
     * @param quantity  the new quantity (must be > 0)
     * @throws IllegalArgumentException if the product is not in the cart
     */
    public void updateQuantity(String productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(quantity);
                return;
            }
        }
        throw new IllegalArgumentException("Product not found in cart: " + productId);
    }

    /**
     * Applies a percentage discount to the current total and returns the discounted total.
     * The discount is applied <em>on top of</em> the raw subtotal; it does not persist
     * between calls (i.e., calling this method twice with 10% does not compound discounts).
     *
     * <p><em>Pre-condition:</em> {@code 0 <= discountRate <= 100}<br>
     * <em>Post-condition:</em> returned value &lt;= {@link #total()} when
     * {@code discountRate > 0}.
     *
     * @param discountRate percentage discount to apply, in [0, 100]
     * @return the total after applying the discount
     */
    public double applyDiscount(double discountRate) {
        // pre-condition
        assert discountRate >= 0 && discountRate <= 100 : "applyDiscount pre: rate must be in [0,100]";

        double rawTotal = total();
        double discounted = rawTotal - (rawTotal * discountRate / 100);

        // post-condition: any positive discount lowers (or keeps equal when total is 0) the total
        assert discountRate == 0 || discounted <= rawTotal : "applyDiscount post: discounted must be <= rawTotal";
        // invariant
        assert discounted >= 0 : "invariant: discounted total >= 0";
        return discounted;
    }

    /**
     * Returns the sum of all cart-item subtotals, with no discounts applied.
     *
     * @return gross total (>= 0)
     */
    public double total() {
        double sum = 0;
        for (CartItem item : items) {
            sum += item.subtotal();
        }
        return sum;
    }

    /**
     * Returns the number of distinct product lines in the cart (not the total unit count).
     */
    public int itemCount() {
        return items.size();
    }

    /**
     * Returns an unmodifiable view of the cart items.
     */
    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Removes all items from the cart.
     */
    public void clear() {
        items.clear();
    }

    @Override
    public String toString() {
        return String.format("ShoppingCart{items=%d, total=%.2f}", items.size(), total());
    }
}
