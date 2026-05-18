package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Task 3 - Design by Contract (Chapter 4).
 *
 * Verifies the pre-/post-conditions and invariants added to ShoppingCart,
 * PriceCalculator and OrderProcessor. Surefire runs with -ea so violated
 * contracts throw AssertionError.
 */
class ContractTest {

    private static final double EPS = 0.001;

    private ShoppingCart cart;
    private PriceCalculator calculator;
    private Product product;

    @BeforeEach
    void setUp() {
        cart       = new ShoppingCart();
        calculator = new PriceCalculator();
        product    = new Product("P001", "Widget", 10.0, 50);
    }

    // ----- ShoppingCart.addItem pre-conditions -----

    @Test
    void addItem_validInput_doesNotThrow() {
        assertThatCode(() -> cart.addItem(product, 3)).doesNotThrowAnyException();
        assertThat(cart.itemCount()).isEqualTo(1);
    }

    @Test
    void addItem_nullProduct_violatesPreCondition() {
        assertThatThrownBy(() -> cart.addItem(null, 1))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void addItem_zeroQuantity_violatesPreCondition() {
        assertThatThrownBy(() -> cart.addItem(product, 0))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void addItem_negativeQuantity_violatesPreCondition() {
        assertThatThrownBy(() -> cart.addItem(product, -5))
                .isInstanceOf(AssertionError.class);
    }

    // post-condition: line count must increase on a new product
    @Test
    void addItem_newProduct_postIncreasesLineCount() {
        int before = cart.itemCount();
        cart.addItem(product, 2);
        assertThat(cart.itemCount()).isEqualTo(before + 1);
    }

    // ----- ShoppingCart.applyDiscount contracts -----

    @Test
    void applyDiscount_validRate_doesNotThrow() {
        cart.addItem(product, 2);
        assertThatCode(() -> cart.applyDiscount(15)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.001, -1.0, 100.001, 150.0})
    void applyDiscount_outOfRange_violatesPreCondition(double rate) {
        cart.addItem(product, 1);
        assertThatThrownBy(() -> cart.applyDiscount(rate))
                .isInstanceOf(AssertionError.class);
    }

    // post-condition: positive discount must lower the total
    @Test
    void applyDiscount_postLowersTotal_whenRatePositive() {
        cart.addItem(product, 4);
        double before = cart.total();
        double after = cart.applyDiscount(25);
        assertThat(after).isLessThan(before);
    }

    // ----- PriceCalculator.calculate contracts -----

    @Test
    void calculate_validInputs_doesNotThrow() {
        assertThatCode(() -> calculator.calculate(100, 10, 18)).doesNotThrowAnyException();
    }

    @Test
    void calculate_negativeBase_violatesPreCondition() {
        assertThatThrownBy(() -> calculator.calculate(-1, 10, 10))
                .isInstanceOf(AssertionError.class);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, 101, 500})
    void calculate_invalidDiscount_violatesPreCondition(double disc) {
        assertThatThrownBy(() -> calculator.calculate(100, disc, 10))
                .isInstanceOf(AssertionError.class);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.5, 101, 200})
    void calculate_invalidTax_violatesPreCondition(double tax) {
        assertThatThrownBy(() -> calculator.calculate(100, 10, tax))
                .isInstanceOf(AssertionError.class);
    }

    // post-condition: result is non-negative for valid inputs
    @Test
    void calculate_postResultNeverNegative() {
        double r = calculator.calculate(50.0, 100.0, 50.0); // wipes everything
        assertThat(r).isGreaterThanOrEqualTo(0.0);
        assertThat(r).isCloseTo(0.0, within(EPS));
    }

    // ----- ShoppingCart invariant -----

    @Test
    void cartInvariant_totalNeverNegative_acrossOperations() {
        cart.addItem(product, 2);
        assertThat(cart.total()).isGreaterThanOrEqualTo(0);

        cart.addItem(product, 3);
        assertThat(cart.total()).isGreaterThanOrEqualTo(0);

        cart.updateQuantity("P001", 1);
        assertThat(cart.total()).isGreaterThanOrEqualTo(0);

        cart.removeItem("P001");
        assertThat(cart.total()).isGreaterThanOrEqualTo(0);

        cart.clear();
        assertThat(cart.total()).isGreaterThanOrEqualTo(0);
    }

    // ----- OrderProcessor.process post-condition (returned Order matches inputs) -----

    @Test
    void orderProcessor_returnedOrder_matchesCustomerAndTotal() {
        // simple in-line fakes so this test stays in the contract spirit (Mockito comes in Task 5)
        InventoryService inv = (p, q) -> true;
        PaymentGateway   pay = (cid, amt) -> true;
        OrderProcessor processor = new OrderProcessor(inv, pay);

        cart.addItem(product, 2);
        Order order = processor.process("customer-42", cart);

        assertThat(order).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo("customer-42");
        assertThat(order.getTotal()).isCloseTo(20.0, within(EPS));
    }
}
