package shopeasy;

/**
 * Stateless helper that computes a final price from a base price,
 * a discount rate, and a tax rate.
 *
 * <p>Formula:
 * <pre>
 *   discounted = basePrice * (1 - discountRate / 100)
 *   final      = discounted * (1 + taxRate / 100)
 * </pre>
 *
 * <p>Contracts are enforced with {@code assert} statements when the JVM
 * runs with {@code -ea}. See the Javadoc on
 * {@link #calculate(double, double, double)} for the exact pre- and
 * post-conditions.
 */
public class PriceCalculator {

    /**
     * Computes the final price after applying a discount and then tax.
     *
     * <p><em>Pre-conditions:</em>
     * <ul>
     *   <li>{@code basePrice >= 0}</li>
     *   <li>{@code 0 <= discountRate <= 100}</li>
     *   <li>{@code 0 <= taxRate <= 100}</li>
     * </ul>
     *
     * <p><em>Post-condition:</em> result &gt;= 0
     *
     * @param basePrice    the original price before any adjustments (>= 0)
     * @param discountRate the discount percentage to apply, in [0, 100]
     * @param taxRate      the tax percentage to apply after discount, in [0, 100]
     * @return             the final price
     */
    public double calculate(double basePrice, double discountRate, double taxRate) {
        // pre-conditions
        assert basePrice >= 0                            : "calculate pre: basePrice must be >= 0";
        assert discountRate >= 0 && discountRate <= 100  : "calculate pre: discountRate must be in [0,100]";
        assert taxRate >= 0 && taxRate <= 100            : "calculate pre: taxRate must be in [0,100]";

        double discounted = basePrice * (1.0 - discountRate / 100.0);
        double withTax    = discounted + (discounted * taxRate / 100.0);

        // post-condition
        assert withTax >= 0 : "calculate post: result must be >= 0";
        return withTax;
    }

    /**
     * Convenience method: applies only a discount, no tax.
     *
     * @param basePrice    the original price (>= 0)
     * @param discountRate the discount percentage [0, 100]
     * @return             the price after discount
     */
    public double applyDiscountOnly(double basePrice, double discountRate) {
        return calculate(basePrice, discountRate, 0);
    }

    /**
     * Convenience method: applies only tax, no discount.
     *
     * @param basePrice the original price (>= 0)
     * @param taxRate   the tax percentage [0, 100]
     * @return          the price after tax
     */
    public double applyTaxOnly(double basePrice, double taxRate) {
        return calculate(basePrice, 0, taxRate);
    }
}
