package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for issue #2: handling insufficient stock at checkout.
 * The database is mocked so the TV is always reported as short.
 */
class InsufficientStockTest {

    private CustomerModel model;
    private RemoveProductNotifier mockNotifier;

    @BeforeEach
    void setUp() {
        // Runs before every test: a fresh model with a TV and a radio in the trolley
        DatabaseRW mockDatabase = mock(DatabaseRW.class);
        model = new CustomerModel(mockDatabase);
        model.cusView = mock(CustomerView.class);

        Product tv = new Product("0001", "40 inch TV", "0001.jpg", 269.00, 2);
        Product radio = new Product("0002", "DAB Radio", "0002.jpg", 29.99, 100);
        model.getTrolley().addProduct(tv);
        model.getTrolley().addProduct(radio);

        // Stub: the database says the TV is short
        ArrayList<Product> insufficient = new ArrayList<>();
        insufficient.add(tv);
        when(mockDatabase.purchaseStocks(any())).thenReturn(insufficient);

        mockNotifier = mock(RemoveProductNotifier.class);
    }

    @Test
    void shortProductIsRemovedAndOthersStay() {
        model.removeProductNotifier = mockNotifier;
        model.checkOut();

        assertEquals(1, model.getTrolley().getProducts().size());
        assertEquals("0002", model.getTrolley().getProducts().get(0).getProductId());
    }

    @Test
    void popUpIsShownWhenStockIsShort() {
        model.removeProductNotifier = mockNotifier;
        model.checkOut();

        verify(mockNotifier).showRemovalMsg(anyString());
    }

    @Test
    void checkoutWithoutNotifierDoesNotCrash() {
        // removeProductNotifier is left as null, like in tests with no UI
        assertDoesNotThrow(() -> model.checkOut());
    }

    @Test
    void cancelClosesPopUp() {
        model.removeProductNotifier = mockNotifier;
        model.cancel();

        verify(mockNotifier).closeNotifierWindow();
    }
}