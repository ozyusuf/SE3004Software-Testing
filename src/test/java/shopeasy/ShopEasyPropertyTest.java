package shopeasy;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 4 – Property-Based Testing (Chapter 5)
 *
 * <p>Target classes: {@link PriceCalculator}, {@link ShoppingCart}
 *
 * <p>Using jqwik, define and test at least <strong>3 distinct properties</strong>.
 * You must use at least one custom {@code @Provide} method.
 *
 * <h3>Suggested properties (you may use these or design your own)</h3>
 * <ul>
 *   <li><b>Monotonicity</b> – For any fixed base and tax, increasing the discount
 *       rate never increases the final price.</li>
 *   <li><b>Identity</b> – A 0% discount and 0% tax returns exactly the base price.</li>
 *   <li><b>Boundedness</b> – The result is always &gt;= 0.</li>
 *   <li><b>Cart commutativity</b> – Adding product A then B yields the same total
 *       as adding B then A.</li>
 *   <li><b>Discount transitivity</b> – Applying a 10% then another 10% discount via
 *       {@code applyDiscount} is equivalent to a single call with the compounded rate
 *       (think carefully: is this actually true for this implementation?).</li>
 * </ul>
 *
 * <h3>For each property, include a comment that answers:</h3>
 * <ol>
 *   <li>What does this property mean in plain English?</li>
 *   <li>What class of bugs would this property catch?</li>
 * </ol>
 *
 * <h3>If jqwik finds a failing case</h3>
 * Do not just fix the test. Investigate the root cause and explain it in your
 * reflection report (include the counterexample jqwik printed).
 */
class ShopEasyPropertyTest {

    // -----------------------------------------------------------------------
    // TODO: Write your properties below.
    //
    // EXAMPLE STRUCTURE:
    //
    // /**
    //  * Property: The final price is always non-negative.
    //  * Bug class caught: any implementation path that produces a negative result
    //  *                   (e.g., discount > 100 applied to negative base).
    //  */
    // @Property
    // void finalPriceIsNeverNegative(
    //         @ForAll @DoubleRange(min = 0, max = 10_000) double base,
    //         @ForAll @DoubleRange(min = 0, max = 100)   double discount,
    //         @ForAll @DoubleRange(min = 0, max = 100)   double tax) {
    //
    //     PriceCalculator calc = new PriceCalculator();
    //     double result = calc.calculate(base, discount, tax);
    //     assertThat(result).isGreaterThanOrEqualTo(0.0);
    // }
    //
    // // Custom provider example:
    // @Provide
    // Arbitrary<Product> validProducts() {
    //     return Combinators.combine(
    //             Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
    //             Arbitraries.doubles().between(0.01, 500.0)
    //     ).as((name, price) -> new Product("P-" + name, name, price, 100));
    // }
    // -----------------------------------------------------------------------

}
