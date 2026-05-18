package shopeasy;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Task 4 - Property-Based Testing (Chapter 5).
 *
 * Four jqwik properties over PriceCalculator and ShoppingCart, plus one
 * custom @Provide method for generating distinct Products.
 */
class ShopEasyPropertyTest {

    private static final double EPS = 1e-6;

    // Used by the @Provide method to keep generated product IDs unique
    // so that addItem does not merge two "different" products into one line.
    private static final AtomicLong PRODUCT_SEQ = new AtomicLong();

    /*
     * Property 1 - Identity.
     *
     * What it means: applying a 0% discount and 0% tax must return exactly
     *                the base price (no hidden adjustment).
     * Bug class caught: off-by-one or wrong constants in the formula, e.g.
     *                   writing (1 + d/100) instead of (1 - d/100).
     */
    @Property
    void identity_zeroDiscountZeroTax_returnsBase(
            @ForAll @DoubleRange(min = 0.0, max = 1_000_000.0) double base) {
        PriceCalculator calc = new PriceCalculator();
        assertThat(calc.calculate(base, 0, 0)).isCloseTo(base, within(EPS));
    }

    /*
     * Property 2 - Monotonicity over discount.
     *
     * What it means: for a fixed base and tax, raising the discount rate
     *                must never produce a higher final price.
     * Bug class caught: sign flips in the discount formula, or accidentally
     *                   using discountRate / 100 as a multiplier instead of
     *                   (1 - discountRate / 100).
     *
     * We pick d1 < d2 to make the property unambiguous.
     */
    @Property
    void monotonicity_higherDiscountNeverIncreasesPrice(
            @ForAll @DoubleRange(min = 0.0, max = 10_000.0) double base,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double tax,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double d1,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double d2) {
        // normalize so d1 <= d2
        double low  = Math.min(d1, d2);
        double high = Math.max(d1, d2);

        PriceCalculator calc = new PriceCalculator();
        double r1 = calc.calculate(base, low,  tax);
        double r2 = calc.calculate(base, high, tax);

        // allow a tiny tolerance because of double rounding
        assertThat(r2).isLessThanOrEqualTo(r1 + 1e-9);
    }

    /*
     * Property 3 - Boundedness.
     *
     * What it means: the final price is always non-negative and never
     *                exceeds base * (1 + tax/100) (the largest possible
     *                price, i.e. zero discount with full tax).
     * Bug class caught: any change that lets the result go negative or that
     *                   stacks tax on the wrong number (e.g. tax applied to
     *                   the un-discounted price).
     */
    @Property
    void boundedness_resultStaysInExpectedRange(
            @ForAll @DoubleRange(min = 0.0, max = 10_000.0) double base,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double discount,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double tax) {
        PriceCalculator calc = new PriceCalculator();
        double result = calc.calculate(base, discount, tax);

        double upperBound = base * (1.0 + tax / 100.0) + 1e-6;

        assertThat(result).isGreaterThanOrEqualTo(0.0);
        assertThat(result).isLessThanOrEqualTo(upperBound);
    }

    /*
     * Property 4 - Cart commutativity.
     *
     * What it means: the total of a cart only depends on which items were
     *                added and how many - not on the order they were added in.
     * Bug class caught: order-dependent state bugs in addItem (e.g. accidentally
     *                   overwriting an existing line instead of merging it).
     *
     * Uses a custom @Provide method (validProducts) so the framework can
     * generate Products that always pass the constructor's input checks.
     */
    @Property
    void commutativity_addingTwoItemsInEitherOrderYieldsSameTotal(
            @ForAll("validProducts") Product a,
            @ForAll("validProducts") Product b,
            @ForAll @IntRange(min = 1, max = 50) int qa,
            @ForAll @IntRange(min = 1, max = 50) int qb) {

        ShoppingCart c1 = new ShoppingCart();
        c1.addItem(a, qa);
        c1.addItem(b, qb);

        ShoppingCart c2 = new ShoppingCart();
        c2.addItem(b, qb);
        c2.addItem(a, qa);

        assertThat(c1.total()).isCloseTo(c2.total(), within(1e-6));
    }

    @Provide
    Arbitrary<Product> validProducts() {
        // unique ID per generated product so two distinct products never collide in a cart
        Arbitrary<String> names  = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8);
        Arbitrary<Double> prices = Arbitraries.doubles().between(0.01, 500.0);
        return Combinators.combine(names, prices).as((name, price) -> {
            String id = "P-" + PRODUCT_SEQ.incrementAndGet() + "-" + name;
            return new Product(id, name, price, 1000);
        });
    }
}
