package ci553.happyshop.catalogue;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrganizedTrolleyTest {

    @Test
    void sameProductAddedTwiceIsMergedIntoOneLine() {
        Trolley trolley = new OrganizedTrolley();
        trolley.addProduct(new Product("0001", "40 inch TV", "0001.jpg", 269.00, 100));
        trolley.addProduct(new Product("0001", "40 inch TV", "0001.jpg", 269.00, 100));

        assertEquals(1, trolley.getProducts().size());
        assertEquals(2, trolley.getProducts().get(0).getOrderedQuantity());
    }

    @Test
    void addingSameObjectRepeatedlyDoesNotDoubleQuantity() {
        // Regression test for the doubling bug (1, 2, 4, 8)
        Trolley trolley = new OrganizedTrolley();
        Product searchResult = new Product("0002", "DAB Radio", "0002.jpg", 29.99, 100);
        trolley.addProduct(searchResult);
        trolley.addProduct(searchResult);
        trolley.addProduct(searchResult);

        assertEquals(3, trolley.getProducts().get(0).getOrderedQuantity());
        assertEquals(1, searchResult.getOrderedQuantity()); // original object unchanged
    }

    @Test
    void productsAreSortedById() {
        Trolley trolley = new OrganizedTrolley();
        trolley.addProduct(new Product("0003", "Toaster", "0003.jpg", 19.99, 100));
        trolley.addProduct(new Product("0001", "40 inch TV", "0001.jpg", 269.00, 100));
        trolley.addProduct(new Product("0002", "DAB Radio", "0002.jpg", 29.99, 100));

        assertEquals("0001", trolley.getProducts().get(0).getProductId());
        assertEquals("0002", trolley.getProducts().get(1).getProductId());
        assertEquals("0003", trolley.getProducts().get(2).getProductId());
    }
}