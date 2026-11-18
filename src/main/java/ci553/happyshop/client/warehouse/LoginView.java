package ci553.happyshop.client.warehouse;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The login window shown before the Warehouse client opens.
 */
public class LoginView {
    public LoginController controller;

    private Stage window;
    private TextField tfUsername;
    private PasswordField pfPassword; // shows dots instead of the password
    private Label lbMessage;

    /**
     * Builds and shows the login window.
     */
    public void start(Stage window) {
        this.window = window;

        Label laTitle = new Label("Warehouse staff login");
        laTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        tfUsername = new TextField();
        tfUsername.setPromptText("Username");

        pfPassword = new PasswordField();
        pfPassword.setPromptText("Password");

        Button btnLogin = new Button("Log in");
        btnLogin.setDefaultButton(true); // pressing Enter also logs in
        btnLogin.setOnAction(e -> {
            controller.doLogin(tfUsername.getText(), pfPassword.getText());
            pfPassword.clear(); // never leave the password sitting in the box
        });

        lbMessage = new Label("Please log in.");
        lbMessage.setWrapText(true);

        VBox root = new VBox(10, laTitle, tfUsername, pfPassword, btnLogin, lbMessage);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        window.setScene(new Scene(root, 320, 240));
        window.setTitle("🔒 HappyShop Warehouse Login");
        window.show();
    }

    /** Shows a message under the login button. */
    void showMessage(String message) {
        lbMessage.setText(message);
    }

    /** Closes the login window. */
    void close() {
        window.close();
    }
}