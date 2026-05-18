package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Task 5 - Mocks and Stubs (Chapter 6).
 *
 * Mocks InventoryService and PaymentGateway so OrderProcessor can be
 * tested in isolation from any real backend.
 */
@ExtendWith(MockitoExtension.class)
class OrderProcessorMockTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private OrderProcessor orderProcessor;

    private ShoppingCart cart;
    private Product widget;
    private Product gadget;

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        widget = new Product("P001", "Widget", 25.0, 100);
        gadget = new Product("P002", "Gadget", 40.0, 100);
    }

    // 1. Happy path - inventory OK, payment OK -> Order returned, payment was charged once.
    @Test
    void process_inventoryOkAndPaymentOk_returnsOrder() {
        cart.addItem(widget, 2);  // total = 50

        when(inventoryService.isAvailable(widget, 2)).thenReturn(true);
        when(paymentGateway.charge("customer-1", 50.0)).thenReturn(true);

        Order order = orderProcessor.process("customer-1", cart);

        assertThat(order).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo("customer-1");
        assertThat(order.getTotal()).isEqualTo(50.0);
        assertThat(order.getItems()).hasSize(1);
        verify(paymentGateway, times(1)).charge("customer-1", 50.0);
    }

    // 2. Inventory failure -> null returned, payment is NEVER attempted.
    @Test
    void process_inventoryUnavailable_returnsNull_andDoesNotCharge() {
        cart.addItem(widget, 5);

        when(inventoryService.isAvailable(widget, 5)).thenReturn(false);

        Order order = orderProcessor.process("customer-2", cart);

        assertThat(order).isNull();
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
    }

    // 3. Payment failure - inventory OK but the gateway declines -> null returned.
    @Test
    void process_paymentDeclined_returnsNull() {
        cart.addItem(widget, 1);  // total = 25

        when(inventoryService.isAvailable(widget, 1)).thenReturn(true);
        when(paymentGateway.charge("customer-3", 25.0)).thenReturn(false);

        Order order = orderProcessor.process("customer-3", cart);

        assertThat(order).isNull();
        verify(paymentGateway, times(1)).charge("customer-3", 25.0);
    }

    /*
     * 4. Partial inventory - the cart has two products. The first is available
     *    but the second is not. Expected behaviour: the processor aborts on the
     *    first failed check, returns null, and never charges anything.
     */
    @Test
    void process_partialInventory_secondItemMissing_returnsNullWithoutCharging() {
        cart.addItem(widget, 1);
        cart.addItem(gadget, 2);

        when(inventoryService.isAvailable(widget, 1)).thenReturn(true);
        when(inventoryService.isAvailable(gadget, 2)).thenReturn(false);

        Order order = orderProcessor.process("customer-4", cart);

        assertThat(order).isNull();
        verify(inventoryService).isAvailable(widget, 1);
        verify(inventoryService).isAvailable(gadget, 2);
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
    }

    // 5. Multi-item happy path - inventory is queried for every line, payment runs once.
    @Test
    void process_multipleItems_inventoryCheckedPerLine_chargesOnce() {
        cart.addItem(widget, 1);  // 25
        cart.addItem(gadget, 1);  // 40 -> total 65

        when(inventoryService.isAvailable(widget, 1)).thenReturn(true);
        when(inventoryService.isAvailable(gadget, 1)).thenReturn(true);
        when(paymentGateway.charge("customer-5", 65.0)).thenReturn(true);

        Order order = orderProcessor.process("customer-5", cart);

        assertThat(order).isNotNull();
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getTotal()).isEqualTo(65.0);
        verify(inventoryService, times(2)).isAvailable(any(Product.class), anyInt());
        verify(paymentGateway, times(1)).charge(eq("customer-5"), eq(65.0));
    }

    // 6. Empty cart - precondition guard: should throw and never touch the dependencies.
    @Test
    void process_emptyCart_throwsAndDoesNotTouchDependencies() {
        assertThatThrownBy(() -> orderProcessor.process("customer-6", cart))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");

        verifyNoInteractions(inventoryService);
        verifyNoInteractions(paymentGateway);
    }
}
