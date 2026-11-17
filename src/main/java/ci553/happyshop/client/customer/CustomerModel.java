package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Order;
import ci553.happyshop.catalogue.OrganizedTrolley;
import ci553.happyshop.catalogue.Product;
import ci553.happyshop.catalogue.Trolley;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.utility.StorageLocation;
import ci553.happyshop.utility.ProductListFormatter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Customer side of the shop: search, trolley (organized, with remove and change quantity)
// and checkout, including handling products that are out of stock.
public class CustomerModel {
    public CustomerView cusView; //part of MVC
    public RemoveProductNotifier removeProductNotifier; // shows a pop-up when items are removed at checkout
    private DatabaseRW databaseRW; //Interface type, not specific implementation
    //Benefits: Flexibility: Easily change the database implementation.

    private Product searchResult = null; // product found from search
    private OrganizedTrolley trolley = new OrganizedTrolley(); // holds the products currently in the customer's trolley

    // Data passed to CustomerView for UI updates (including search, trolley and receipt page)
    private String searchResultImageName = "imageHolder.jpg";
    // Image file name representing the current search result.
    // - Product image if a product is found
    // - Default placeholder image otherwise
    private String searchResultProductText = "No product has been searched yet";
    // Textual representation of the product from the search result.
    // - Shows product details (e.g. "0001, 40-inch TV, ...") if found
    // - Otherwise shows a status message
    private String trolleyContentsText = "";
    // Textual representation of current trolley contents,
    // used for display in the Trolley page
    private String receiptContentsText = "";
    // Textual representation of the generated receipt after checkout,
    // used for display in the Receipt page

    // Dependency Injection via constructor
    public CustomerModel(DatabaseRW databaseRW) {
        this.databaseRW = databaseRW;
    }

    //SELECT productID, description, image, unitPrice,inStock quantity
    void search() {
        String productId = cusView.tfId.getText().trim();
        if (!productId.isEmpty()) {
            searchResult = databaseRW.searchProductById(productId); //search database
            if (searchResult != null && searchResult.getStockQuantity() > 0) {
                double unitPrice = searchResult.getUnitPrice();
                String description = searchResult.getProductDescription();
                int stock = searchResult.getStockQuantity();

                String baseInfo = String.format("Product_Id: %s\n%s,\nPrice: £%.2f", productId, description, unitPrice);
                String quantityInfo = stock < 100 ? String.format("\n%d units left.", stock) : "";
                searchResultProductText = baseInfo + quantityInfo;
                System.out.println(searchResultProductText);
            } else {
                searchResult = null;
                searchResultProductText = "No Product was found with ID " + productId;
                System.out.println("No Product was found with ID " + productId);
            }
        } else {
            searchResult = null;
            searchResultProductText = "Please type ProductID";
            System.out.println("Please type ProductID.");
        }
        updateView();
    }

    void addToTrolley() {
        if (searchResult != null) {
            // Add the product, unless that would go over the available stock
            int alreadyInTrolley = 0;
            for (Product p : trolley.getProducts()) {
                if (p.getProductId().equals(searchResult.getProductId())) {
                    alreadyInTrolley = p.getOrderedQuantity();
                }
            }

            // Same stock rule as the + button
            if (alreadyInTrolley + searchResult.getOrderedQuantity() > searchResult.getStockQuantity()) {
                searchResultProductText = "Sorry, only " + searchResult.getStockQuantity()
                        + " of " + searchResult.getProductDescription() + " in stock.";
            } else {
                trolley.addProduct(searchResult);
            }
            // Build a formatted string so the trolley contents can be displayed in the UI
            trolleyContentsText = ProductListFormatter.buildString(trolley.getProducts());
        } else {
            searchResultProductText = "Please search and get an available product before adding to trolley";
            System.out.println("must search and get an available product before add to trolley");
        }
        receiptContentsText = ""; // Clear receipt to switch back to trolleyPage (receipt shows only when not empty)
        updateView();
    }

    public void checkOut() {
        if (!trolley.getProducts().isEmpty()) {
            // Group the products in the trolley by productId to optimize stock checking
            // Check the database for sufficient stock for all products in the trolley.
            // If any products are insufficient, the update will be rolled back.
            // If all products are sufficient, the database will be updated, and insufficientProducts will be empty.
            // Note: the trolley is now organized (OrganizedTrolley), so grouping is no longer
            // strictly needed; it is kept as a safety check.
            ArrayList<Product> groupedProducts = groupProductsById(trolley.getProducts());
            ArrayList<Product> insufficientProducts = databaseRW.purchaseStocks(groupedProducts);

            if (insufficientProducts.isEmpty()) { // If stock is sufficient for all products
                //get OrderHub and tell it to make a new Order
                OrderHub orderHub = OrderHub.getOrderHub();
                Order theOrder = orderHub.newOrder(trolley.getProducts());
                trolley.clear();
                if (removeProductNotifier != null) {
                    removeProductNotifier.closeNotifierWindow();
                }
                trolleyContentsText = "";
                searchResult = null;
                searchResultProductText = "Thank you for shopping with us.";
                receiptContentsText = String.format(
                        "Order_ID: %s\nOrdered_Date_Time: %s\n%s",
                        theOrder.getOrderId(),
                        theOrder.getOrderedDateTime(),
                        ProductListFormatter.buildString(theOrder.getProductList())
                );
                System.out.println(receiptContentsText);
            } else { // Some products have insufficient stock — build an error message to inform the customer
                StringBuilder errorMsg = new StringBuilder();
                for (Product p : insufficientProducts) {
                    errorMsg.append("\u2022 " + p.getProductId()).append(", ")
                            .append(p.getProductDescription()).append(" (Only ")
                            .append(p.getStockQuantity()).append(" available, ")
                            .append(p.getOrderedQuantity()).append(" requested)\n");
                }
                searchResult = null;

                // 1. Remove products with insufficient stock from the trolley
                for (Product p : insufficientProducts) {
                    trolley.removeProduct(p.getProductId());
                }
                trolleyContentsText = ProductListFormatter.buildString(trolley.getProducts());

                // 2. Show a pop-up instead of changing the search text
                //    (only if a notifier exists, so tests without a UI don't crash)
                if (removeProductNotifier != null) {
                    removeProductNotifier.showRemovalMsg(errorMsg.toString());
                }
                searchResultProductText = "Some items were removed from your trolley.";
                System.out.println("stock is not enough");
            }
        } else {
            trolleyContentsText = "Your trolley is empty";
            System.out.println("Your trolley is empty");
        }
        updateView();
    }

    /**
     * Groups products by their productId to optimize database queries and updates.
     * By grouping products, we can check the stock for a given `productId` once, rather than repeatedly
     */
    private ArrayList<Product> groupProductsById(ArrayList<Product> proList) {
        Map<String, Product> grouped = new HashMap<>();
        for (Product p : proList) {
            String id = p.getProductId();
            if (grouped.containsKey(id)) {
                Product existing = grouped.get(id);
                existing.setOrderedQuantity(existing.getOrderedQuantity() + p.getOrderedQuantity());
            } else {
                // Make a shallow copy to avoid modifying the original
                grouped.put(id, new Product(p.getProductId(), p.getProductDescription(),
                        p.getProductImageName(), p.getUnitPrice(), p.getOrderedQuantity(), p.getStockQuantity()));
            }
        }
        return new ArrayList<>(grouped.values());
    }

    /**
     * Changes the quantity of the selected trolley line by one.
     * Going below 1 removes the product; going above the known stock is blocked.
     *
     * @param index  the selected row (0 = first product), or -1 if nothing is selected
     * @param change +1 to add one, -1 to take one away
     */
    void changeQuantity(int index, int change) {
        if (index < 0 || index >= trolley.getProducts().size()) {
            searchResultProductText = "Please click a product in your trolley first.";
        } else {
            Product p = trolley.getProducts().get(index);
            int newQuantity = p.getOrderedQuantity() + change;

            if (newQuantity < 1) {
                // edge case 1: going below 1 removes the product
                trolley.removeProduct(p.getProductId());
            } else if (newQuantity > p.getStockQuantity()) {
                // edge case 2: block going above the stock
                searchResultProductText = "Sorry, only " + p.getStockQuantity()
                        + " of " + p.getProductDescription() + " in stock.";
            } else {
                p.setOrderedQuantity(newQuantity);
            }
            trolleyContentsText = trolley.getProducts().isEmpty()
                    ? ""
                    : ProductListFormatter.buildString(trolley.getProducts());
        }
        updateView();
    }

    /**
     * Removes the trolley line the customer selected in the list.
     *
     * @param index the position of the selected row (0 = first product);
     *              -1 means nothing is selected
     */
    void removeFromTrolley(int index) {
        if (index >= 0 && index < trolley.getProducts().size()) {
            String productId = trolley.getProducts().get(index).getProductId();
            trolley.removeProduct(productId);
            trolleyContentsText = trolley.getProducts().isEmpty()
                    ? ""
                    : ProductListFormatter.buildString(trolley.getProducts());
        } else {
            searchResultProductText = "Please click a product in your trolley first.";
        }
        updateView();
    }

    void cancel() {
        trolley.clear();
        if (removeProductNotifier != null) {
            removeProductNotifier.closeNotifierWindow();
        }
        trolleyContentsText = "";
        updateView();
    }

    void closeReceipt() {
        receiptContentsText = "";
    }

    void updateView() {
        if (searchResult != null) {
            searchResultImageName = searchResult.getProductImageName();
            String relativeImageUrl = StorageLocation.imageFolder + searchResultImageName; //relative file path, eg images/0001.jpg
            // Get the full absolute path to the image
            Path imageFullPath = Paths.get(relativeImageUrl).toAbsolutePath();
            searchResultImageName = imageFullPath.toUri().toString(); //get the image full Uri then convert to String
            System.out.println("Image absolute path: " + imageFullPath); // Debugging to ensure path is correct
        } else {
            searchResultImageName = "imageHolder.jpg";
        }
        cusView.update(searchResultImageName, searchResultProductText, trolleyContentsText, receiptContentsText);
    }
    // extra notes:
    //Path.toUri(): Converts a Path object (a file or a directory path) to a URI object.
    //File.toURI(): Converts a File object (a file on the filesystem) to a URI object

    //for test
    public Trolley getTrolley() {
        return trolley;
    }
}
