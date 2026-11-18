package ci553.happyshop.client.warehouse;

/**
 * Passes the login button click to the model, shows the result,
 * and opens the Warehouse window after a successful login.
 */
public class LoginController {
    public LoginModel model;
    public LoginView view;
    public Runnable onSuccess; // what to do after a correct login (open the Warehouse window)

    /**
     * Tries to log in with the details typed into the window.
     */
    void doLogin(String username, String password) {
        boolean success = model.login(username, password);
        view.showMessage(model.getMessage());
        if (success) {
            view.close();
            onSuccess.run();
        }
    }
}