package poo.rs;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class loginController implements Initializable {
    public TextField tname;
    public PasswordField tpwd;
    public Button btnLogin;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnLogin.setOnAction(actionEvent -> login());
    }

    public void login() {
        try (Connection con = ConnectionDB.getConnection();
             PreparedStatement st = con.prepareStatement("SELECT * FROM users WHERE USERNAME = ? AND PASSWORD = ?")) {
            st.setString(1, tname.getText());
            st.setString(2, tpwd.getText());

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    // Login successful, switch to the form interface
                    switchToForm(rs.getInt("ID"));
                } else {
                    showAlert("Login Error", "Invalid username or password.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while accessing the database.");
        }
    }

    public void switchToSignup() {
        switchScene("/poo/rs/FXML/signup.fxml");
    }

    public void switchToForm(int userId) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/poo/rs/FXML/form.fxml"));
        switchScene(loader, userId);
    }

    private void switchScene(String scenePath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(scenePath));
            Parent root = loader.load();
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while loading the scene.");
        }
    }

    private void switchScene(FXMLLoader loader, int userId) {
        try {
            Parent root = loader.load();
            FormController formController = loader.getController();
            formController.setUserId(userId);
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while loading the scene.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
