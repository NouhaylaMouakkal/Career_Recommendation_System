package poo.rs;

import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class signupController implements Initializable {
    public TextField nname;
    public PasswordField npwd;
    public PasswordField ncpwd;
    public Button btnSignup;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnSignup.setOnAction(actionEvent -> signup());
    }

    public void signup() {
        if (passwordsMatch()) {
            try (Connection con = ConnectionDB.getConnection();
                 PreparedStatement st = con.prepareStatement("INSERT INTO users (USERNAME, PASSWORD) VALUES (?, ?)")) {
                st.setString(1, nname.getText());
                st.setString(2, npwd.getText());

                int rowsAffected = st.executeUpdate();

                if (rowsAffected > 0) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Sign Up Successfully", ButtonType.OK);
                    alert.show();

                    // Switch to the login interface after successful signup
                    switchToLogin();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Sign Up Error", ButtonType.OK);
                    alert.show();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Passwords do not match", ButtonType.OK);
            alert.show();
        }
    }

    private boolean passwordsMatch() {
        String password = npwd.getText();
        String confirmPassword = ncpwd.getText();
        return password.equals(confirmPassword);
    }

    // Switch to the login interface
    public void switchToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/poo/rs/FXML/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnSignup.getScene().getWindow(); // Get the current stage
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
