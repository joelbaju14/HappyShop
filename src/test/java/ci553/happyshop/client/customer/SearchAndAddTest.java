package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

/**
 * Tests for issue #6: searching for a product and adding it to the trolley.
 * search() used to read the ID straight from the window, so these could
 * only be tested by hand. search(String) takes the ID as a parameter instead.
 */
class SearchAndAddTest {

    private CustomerModel model;
    private DatabaseRW db;

    @BeforeEach
    void setUp() throws Exception {
        db = mock(DatabaseRW.class);
        model = new CustomerModel(db);
        model.cusView = mock(CustomerView.class);
        // the fake database knows one product: a DAB Radio with 3 in stock
        when(db.searchProductById("0002"))
                .thenReturn(new Product("0002", "DAB Radio", "0002.jpg", 29.99, 3));
    }

    /** Checks the message the customer would see on screen. */
    private void messageShown(String text) {
        verify(model.cusView, atLeastOnce())
                .update(anyString(), contains(text), anyString(), anyString());
    }

    @Test
    void foundProductCanBeAdded() {
        model.search("0002");
        model.addToTrolley();

        assertEquals(1, model.getTrolley().getProducts().size());
        assertEquals("0002", model.getTrolley().getProducts().get(0).getProductId());
    }

    @Test
    void unknownIdAddsNothing() {
        model.search("9999"); // the mock returns null for any other ID
        model.addToTrolley();

        assertTrue(model.getTrolley().getProducts().isEmpty());
        messageShown("No Product was found");
    }

    @Test
    void emptyIdAddsNothing() {
        model.search("");
        model.addToTrolley();

        assertTrue(model.getTrolley().getProducts().isEmpty());
        messageShown("Please type ProductID");
    }

    @Test
    void addToTrolleyStopsAtStockLimit() {
        model.search("0002");
        model.addToTrolley(); // 1
        model.addToTrolley(); // 2
        model.addToTrolley(); // 3
        model.addToTrolley(); // blocked: only 3 in stock

        assertEquals(3, model.getTrolley().getProducts().get(0).getOrderedQuantity());
        messageShown("Sorry, only 3");
    }

    @Test
    void outOfStockProductCannotBeAdded() throws Exception {
        when(db.searchProductById("0003"))
                .thenReturn(new Product("0003", "Toaster", "0003.jpg", 19.99, 0));

        model.search("0003");
        model.addToTrolley();

        assertTrue(model.getTrolley().getProducts().isEmpty());
    }
}