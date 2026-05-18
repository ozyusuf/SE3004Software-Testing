package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 3 – Design by Contract (Chapter 4)
 *
 * <p>This task has two parts:
 *
 * <h3>Part A – Add contracts to production code</h3>
 * Open {@link ShoppingCart} and {@link PriceCalculator} and add {@code assert}
 * statements for the pre-conditions and post-conditions described in their Javadoc.
 * Note: assertions are enabled via {@code -ea} in Maven Surefire (already configured
 * in {@code pom.xml}).
 *
 * <p>Contracts to implement:
 * <ul>
 *   <li><b>ShoppingCart.addItem</b>: pre — {@code product != null}, {@code quantity > 0};
 *       post — {@code itemCount()} increased or product quantity updated.</li>
 *   <li><b>ShoppingCart.applyDiscount</b>: pre — {@code 0 <= discountRate <= 100};
 *       post — result &lt;= {@code total()} when {@code discountRate > 0}.</li>
 *   <li><b>PriceCalculator.calculate</b>: pre — {@code basePrice >= 0},
 *       {@code 0 <= discountRate <= 100}, {@code 0 <= taxRate <= 100};
 *       post — result {@code >= 0}.</li>
 *   <li><b>ShoppingCart invariant</b>: {@code total() >= 0} after any operation.</li>
 * </ul>
 *
 * <h3>Part B – Write contract tests</h3>
 * Write tests below that:
 * <ol>
 *   <li>Verify contracts hold for valid inputs (positive tests).</li>
 *   <li>Verify contracts are violated ({@code AssertionError}) for invalid inputs (negative tests).</li>
 * </ol>
 *
 * <p>Use {@code assertThatThrownBy(...).isInstanceOf(AssertionError.class)} to test violations.
 */
class ContractTest {

    private ShoppingCart cart;
    private PriceCalculator calculator;
    private Product product;

    @BeforeEach
    void setUp() {
        cart       = new ShoppingCart();
        calculator = new PriceCalculator();
        product    = new Product("P001", "Widget", 10.0, 50);
    }

    // -----------------------------------------------------------------------
    // TODO: Write your contract tests below.
    //
    // EXAMPLE — pre-condition violation (fill in the correct assertion):
    //
    // @Test
    // void addItem_nullProduct_shouldViolatePreCondition() {
    //     assertThatThrownBy(() -> cart.addItem(null, 1))
    //             .isInstanceOf(AssertionError.class);
    // }
    //
    // EXAMPLE — pre-condition holds (valid input):
    //
    // @Test
    // void addItem_validInput_shouldNotThrow() {
    //     assertThatCode(() -> cart.addItem(product, 3)).doesNotThrowAnyException();
    // }
    // -----------------------------------------------------------------------

}
