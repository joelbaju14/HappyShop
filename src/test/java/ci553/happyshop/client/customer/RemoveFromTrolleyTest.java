package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Tests for issue #3: removing a selected line from the trolley.
 * The list shows 3 products (rows 0-2), then a dashed line (row 3) and Total (row 4).
 */
class RemoveFromTrolleyTest {

    private CustomerModel model;

    @BeforeEach
    void setUp() {
        model = new CustomerModel(mock(DatabaseRW.class));
        model.cusView = mock(CustomerView.class);
        model.getTrolley().addProduct(new Product("0001", "40 inch TV", "0001.jpg", 269.00, 100));
        model.getTrolley().addProduct(new Product("0002", "DAB Radio", "0002.jpg", 29.99, 100));
        model.getTrolley().addProduct(new Product("0006", "i Watch", "0006.jpg", 100.00, 100));
    }

    @Test
    void selectedProductIsRemoved() {
        model.removeFromTrolley(1); // the DAB Radio row

        assertEquals(2, model.getTrolley().getProducts().size());
        assertEquals("0001", model.getTrolley().getProducts().get(0).getProductId());
        assertEquals("0006", model.getTrolley().getProducts().get(1).getProductId());
    }

    @Test
    void nothingSelectedRemovesNothing() {
        model.removeFromTrolley(-1); // -1 = no row selected

        assertEquals(3, model.getTrolley().getProducts().size());
    }

    @Test
    void totalRowRemovesNothing() {
        model.removeFromTrolley(4); // row 4 is the Total line, not a product

        assertEquals(3, model.getTrolley().getProducts().size());
    }

    @Test
    void removingEveryProductEmptiesTrolley() {
        model.removeFromTrolley(0);
        model.removeFromTrolley(0);
        model.removeFromTrolley(0);

        assertTrue(model.getTrolley().getProducts().isEmpty());
    }
}