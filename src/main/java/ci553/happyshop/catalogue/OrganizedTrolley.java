package ci553.happyshop.catalogue;

import java.util.Collections;

public class OrganizedTrolley extends Trolley {

    @Override
    public void addProduct(Product product) {

        // 1. Check if a product with the same ID is already in the trolley
        for (Product p : getProducts()) {
            if (p.getProductId().equals(product.getProductId())) {
                // Same product found: add the new quantity onto the existing one
                p.setOrderedQuantity(p.getOrderedQuantity() + product.getOrderedQuantity());
                Collections.sort(getProducts());
                return; // stop here so it isn't added again as a new line
            }
        }

        // 2. No match found: add it as a new line
        // Add a copy, so the trolley's product is a separate object from searchResult
        Product copy = new Product(
                product.getProductId(),
                product.getProductDescription(),
                product.getProductImageName(),
                product.getUnitPrice(),
                product.getOrderedQuantity(),
                product.getStockQuantity());
        super.addProduct(copy);

        // 3. Keep the trolley sorted by product ID
        Collections.sort(getProducts());
    }
}