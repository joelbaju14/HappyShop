package ci553.happyshop.client.customer;

public class CustomerController {
    public CustomerModel cusModel;

    public void doAction(String action) {
        switch (action) {
            case "Search":
                cusModel.search();
                break;
            case "Add to Trolley":
                cusModel.addToTrolley();
                break;
            case "Cancel":
                cusModel.cancel();
                break;
            case "Check Out":
                cusModel.checkOut();
                break;
            case "OK & Close":
                cusModel.closeReceipt();
                break;
        }
    }
    /**
     * Passes the customer's selected trolley row to the model to remove it.
     * @param selectedIndex the row the customer clicked, or -1 if none
     */
    public void doRemove(int selectedIndex) {
        cusModel.removeFromTrolley(selectedIndex);
    }
    /**
     * Passes the selected trolley row and the change (+1 or -1) to the model.
     * @param selectedIndex the row the customer clicked, or -1 if none
     * @param change        +1 for the + button, -1 for the - button
     */
    public void doChangeQuantity(int selectedIndex, int change) {
        cusModel.changeQuantity(selectedIndex, change);
    }
}
