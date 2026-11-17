package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Tests for issue #4: changing a trolley line's quantity with + and -.
 * The DAB Radio has only 3 in stock, so + must stop there.
 */
class ChangeQuantityTest {

    private CustomerModel model;

    @BeforeEach
    void setUp() {
        model = new CustomerModel(mock(DatabaseRW.class));
        model.cusView = mock(CustomerView.class);
        // one DAB Radio in the trolley, with 3 in stock
        model.getTrolley().addProduct(new Product("0002", "DAB Radio", "0002.jpg", 29.99, 3));
    }

    private int radioQuantity() {
        return model.getTrolley().getProducts().get(0).getOrderedQuantity();
    }

    @Test
    void plusIncreasesQuantityByOne() {
        model.changeQuantity(0, +1);

        assertEquals(2, radioQuantity());
    }

    @Test
    void plusStopsAtStockLimit() {
        model.changeQuantity(0, +1); // 2
        model.changeQuantity(0, +1); // 3
        model.changeQuantity(0, +1); // blocked: only 3 in stock

        assertEquals(3, radioQuantity());
    }

    @Test
    void minusDecreasesQuantityByOne() {
        model.changeQuantity(0, +1); // 2
        model.changeQuantity(0, -1); // back to 1

        assertEquals(1, radioQuantity());
    }

    @Test
    void minusAtOneRemovesProduct() {
        model.changeQuantity(0, -1); // 1 -> 0, so the radio is removed

        assertTrue(model.getTrolley().getProducts().isEmpty());
    }

    @Test
    void nothingSelectedChangesNothing() {
        model.changeQuantity(-1, +1); // -1 = no row selected

        assertEquals(1, radioQuantity());
    }
}