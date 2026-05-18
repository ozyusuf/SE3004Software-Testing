package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Task 2 - Structural Testing & Code Coverage (Chapter 3).
 *
 * Goal: achieve >= 80% branch coverage on ShoppingCart.
 *
 * Branches we deliberately target:
 *   addItem        - existing-product branch vs new-product branch
 *   removeItem     - product found vs not found (removeIf predicate true/false)
 *   updateQuantity - quantity <= 0 guard, found branch, not-found exception
 *   applyDiscount  - rate = 0 path and rate > 0 path
 *   total          - empty loop body vs non-empty
 */
class ShoppingCartStructuralTest {

    private static final double EPS = 0.001;

    private ShoppingCart cart;
    private Product apple;
    private Product banana;

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        apple  = new Product("P001", "Apple",  1.50, 100);
        banana = new Product("P002", "Banana", 0.80, 50);
    }

    // Empty cart: total loop never iterates, itemCount is zero.
    @Test
    void newCart_isEmpty_totalIsZero() {
        assertThat(cart.itemCount()).isEqualTo(0);
        assertThat(cart.total()).isCloseTo(0.0, within(EPS));
        assertThat(cart.getItems()).isEmpty();
    }

    // addItem - new product branch (the for loop falls through and a new CartItem is created).
    @Test
    void addItem_newProduct_addsLineAndUpdatesTotal() {
        cart.addItem(apple, 3);

        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isCloseTo(4.5, within(EPS));
    }

    // addItem - existing product branch (loop finds the product and merges quantities).
    @Test
    void addItem_sameProductTwice_mergesIntoOneLine() {
        cart.addItem(apple, 2);
        cart.addItem(apple, 3);

        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(cart.total()).isCloseTo(7.5, within(EPS));
    }

    @Test
    void addItem_twoDifferentProducts_keepsTwoLines() {
        cart.addItem(apple, 2);
        cart.addItem(banana, 4);

        assertThat(cart.itemCount()).isEqualTo(2);
        // 2 * 1.50 + 4 * 0.80 = 6.20
        assertThat(cart.total()).isCloseTo(6.20, within(EPS));
    }

    // removeItem - product is in the cart so removeIf returns true.
    @Test
    void removeItem_existingProduct_removesLine() {
        cart.addItem(apple, 1);
        cart.addItem(banana, 2);

        cart.removeItem("P001");

        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.getItems().get(0).getProduct().getId()).isEqualTo("P002");
    }

    // removeItem - id not present so removeIf returns false on every element.
    @Test
    void removeItem_unknownProduct_isNoOp() {
        cart.addItem(apple, 1);

        cart.removeItem("NOPE");

        assertThat(cart.itemCount()).isEqualTo(1);
    }

    // updateQuantity - happy path: the item is found and updated.
    @Test
    void updateQuantity_existingProduct_updatesIt() {
        cart.addItem(apple, 1);

        cart.updateQuantity("P001", 7);

        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(7);
        assertThat(cart.total()).isCloseTo(10.5, within(EPS));
    }

    // updateQuantity - not found branch should throw.
    @Test
    void updateQuantity_unknownProduct_throws() {
        cart.addItem(apple, 1);

        assertThatThrownBy(() -> cart.updateQuantity("X", 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    // updateQuantity - guard clause for non-positive quantity.
    @ParameterizedTest
    @ValueSource(ints = {0, -1, -50})
    void updateQuantity_nonPositive_throws(int qty) {
        cart.addItem(apple, 1);

        assertThatThrownBy(() -> cart.updateQuantity("P001", qty))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // applyDiscount - rate = 0 branch (the multiplication by 0 / 100 = 0 path).
    @Test
    void applyDiscount_zeroPercent_returnsRawTotal() {
        cart.addItem(apple, 2);

        double discounted = cart.applyDiscount(0);

        assertThat(discounted).isCloseTo(cart.total(), within(EPS));
    }

    // applyDiscount - rate > 0 branch (50% off should halve the total).
    @Test
    void applyDiscount_fiftyPercent_halvesTotal() {
        cart.addItem(apple, 4);  // total = 6.0

        double discounted = cart.applyDiscount(50);

        assertThat(discounted).isCloseTo(3.0, within(EPS));
    }

    // applyDiscount on an empty cart - total is 0 so discounted is 0 too.
    @Test
    void applyDiscount_emptyCart_isZero() {
        double discounted = cart.applyDiscount(20);

        assertThat(discounted).isCloseTo(0.0, within(EPS));
    }

    @Test
    void clear_emptiesCart() {
        cart.addItem(apple, 1);
        cart.addItem(banana, 1);

        cart.clear();

        assertThat(cart.itemCount()).isEqualTo(0);
        assertThat(cart.total()).isCloseTo(0.0, within(EPS));
    }

    // getItems returns an unmodifiable view - mutation must throw.
    @Test
    void getItems_returnsUnmodifiableView() {
        cart.addItem(apple, 1);

        assertThatThrownBy(() -> cart.getItems().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    // toString smoke test to cover the format call (also reads itemCount/total branches).
    @Test
    void toString_containsItemCountAndTotal() {
        cart.addItem(apple, 2);
        String s = cart.toString();

        assertThat(s).contains("items=1").contains("3.00");
    }
}
