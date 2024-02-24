package poo.rs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.text.Text;
import javafx.scene.control.Tooltip;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class FormController implements Initializable {

    @FXML
    private TextField hardSkillsTxt;
    @FXML
    private ChoiceBox<String> leveltxt;
    @FXML
    private ListView<String> listView;
    @FXML
    private Label welcomeLabel;

    private int userId;
    private String level;
    private String hardSkills;

    //private VisualizationController visualizationController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String[] items = {"Anglais", "Marketing", "Communication", "Leadership", "Adaptabilité", "Prise de décision",
                "Gestion du temps", "Esprit d'équipe", "Analyse et synthèse", "Problem solving", "Orientation client",
                "Créativité", "Patience", "Autonomie", "Empathie", "Gestion du stress",
                "Collaboration", "Flexibilité", "Pensée critique", "Négociation", "Gestion du changement",
                "Persévérance", "Curiosité"};
        listView.getItems().addAll(items);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.getSelectionModel().selectedItemProperty().addListener(this::selectionChanged);

        // Initialize ChoiceBox items
        leveltxt.getItems().addAll("Bac", "Bac+1", "Bac+2", "Bac+3", "Bac+4", "Bac+5", "Bac+6", "More...");

    }

    /*@FXML
    private void handleChoiceBoxClick(MouseEvent event) {
        ChoiceBox<String> choiceBox = (ChoiceBox<String>) event.getSource();
        String selectedLevel = choiceBox.getSelectionModel().getSelectedItem();
        System.out.println("Selected Level: " + selectedLevel);
        // Add your logic here to handle the selected level
    }*/
    private void handleChoiceBoxAction(ActionEvent event) {
        ChoiceBox<String> choiceBox = (ChoiceBox<String>) event.getSource();
        String selectedLevel = choiceBox.getValue();
        System.out.println("Selected Level: " + selectedLevel);
    }

    private void sendFormDataToServer(String level, String hardSkills, String softSkills) {
        try {
            // Préparez les données utilisateur
            Map<String, Object> userData = new HashMap<>();
            userData.put("level", level);
            userData.put("hard_skills", hardSkills);
            userData.put("soft_skills", softSkills);
            // Convertissez les données utilisateur en JSON
            String jsonData = convertMapToJson(userData);
            // Endpoint de l'API
            String apiEndpoint = "http://localhost:5000/recommend_jobs";
            // Envoyez une requête POST au serveur Flask
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiEndpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // Process API response
            System.out.println("API Response Status Code: " + response.statusCode());
            System.out.println("API Response Body:");
            System.out.println(response.body());

            // Affichez les recommandations dans le graphe
            Map<String, String> jobSalaries = parseJsonSalaries(response.body());
            displayRecommendations(response.body(), jobSalaries);


        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while communicating with the server.");
        }
    }
    private Map<String, String> parseJsonSalaries(String responseBody) {
        try {
            // Utilisez Jackson pour parser la réponse JSON
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, List<Map<String, String>>> jsonMap = objectMapper.readValue(responseBody,
                    new TypeReference<Map<String, List<Map<String, String>>>>() {
                    });

            List<Map<String, String>> results = jsonMap.get("results");

            if (results != null) { // Vérifiez si la liste des résultats n'est pas null
                Map<String, String> jobSalaries = new HashMap<>();
                for (Map<String, String> result : results) {
                    String jobTitle = result.get("job_title");
                    String salary = result.get("annual_salary"); // Utilisez "annual_salary" pour obtenir le salaire
                    jobSalaries.put(jobTitle, salary);
                }
                return jobSalaries;
            } else {
                return new HashMap<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private void displayRecommendations(String responseBody, Map<String, String> jobSalaries) {
        // Parsez la réponse JSON et affichez les recommandations dans le graphe
        Map<String, Double> jobRecommendations = parseJsonResponse(responseBody);
        showJavaFXChart(jobRecommendations, jobSalaries);
    }

    private String convertMapToJson(Map<String, Object> data) {
        try {
            // Utilisez Jackson pour convertir la Map en JSON
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}"; // Gérer les erreurs de conversion de manière appropriée
        }
    }

    private Map<String, Double> parseJsonResponse(String responseBody) {
        try {
            // Utilisez Jackson pour parser la réponse JSON
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, List<Map<String, Object>>> jsonMap = objectMapper.readValue(responseBody,
                    new TypeReference<Map<String, List<Map<String, Object>>>>() {
                    });

            List<Map<String, Object>> results = jsonMap.get("results");
            Map<String, Double> jobRecommendations = new HashMap<>();
            for (Map<String, Object> result : results) {
                String jobTitle = (String) result.get("job_title");
                Double percentage = (Double) result.get("percentage");
                jobRecommendations.put(jobTitle, percentage);
            }
            return jobRecommendations;
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private void showJavaFXChart(Map<String, Double> jobRecommendations, Map<String, String> jobSalaries) {

        BarChart<Number, String> barChart = new BarChart<>(new NumberAxis(), new CategoryAxis());
        barChart.setTitle("Job Recommendations For You ");
        // Création de la série de données
        XYChart.Series<Number, String> series = new XYChart.Series<>();
        // Ajout des données de recommandation à la série
        for (Map.Entry<String, Double> entry : jobRecommendations.entrySet()) {
            String jobTitle = entry.getKey();
            Double percentage = entry.getValue();

            // Ajouter le salaire correspondant
            String salary = jobSalaries.getOrDefault(jobTitle, "N/A");
            String label = jobTitle + " ( " + salary +" )";

            XYChart.Data<Number, String> data = new XYChart.Data<>(percentage, label);
            series.getData().add(data);

            // Use a StackPane for each data node
            StackPane stackPane = new StackPane();
            stackPane.getChildren().add(new Text(String.format("%.2f%%", percentage)));
            data.setNode(stackPane);

            // Display the percentage on the bar
            stackPane.setOnMouseEntered(event ->
                    Tooltip.install(stackPane, new Tooltip(label + "\nPercentage: " + String.format("%.2f%%", percentage))));

        }
        // Ajout de la série au graphique
        barChart.getData().add(series);

        // Personnalisation des couleurs des barres avec CSS
        for (XYChart.Data<Number, String> data : series.getData()) {

            if (data.getXValue().doubleValue() > 60.0) {
                data.getNode().setStyle("-fx-bar-fill: #4CAF50;");
            } else {
                data.getNode().setStyle("-fx-bar-fill: #2196F3;");
            }
        }

        // Création d'une scène et configuration de la scène dans la fenêtre principale
        Scene scene = new Scene(barChart, 900, 600);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Job Recommendations");
        stage.show();
    }
    private void selectionChanged(ObservableValue<? extends String> observable, String oldVal, String newVal) {
        // Handle selection changed if needed
    }
    @FXML
    private void handleSubmit(ActionEvent event) {
        // Use getValue() to get the selected level from the ChoiceBox
        String level = leveltxt.getValue();
        String hardSkills = hardSkillsTxt.getText();

        if (level == null || level.isEmpty() || hardSkills.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        if (!isValidHardSkillsFormat(hardSkills)) {
            showAlert("Error", "Please enter a comma after each hard skill.");
            return;
        }

        ObservableList<String> selectedSoftSkills = listView.getSelectionModel().getSelectedItems();
        if (selectedSoftSkills.isEmpty()) {
            showAlert("Error", "Please select at least one soft skill.");
            return;
        }

        welcomeLabel.setText("Welcome  " + userId);// Set it only once
        // The following line is unnecessary and can be removed:
        System.out.println("User ID: " + userId);
        System.out.println("Level: " + level);
        System.out.println("Hard Skills: " + hardSkills);
        String softSkills = String.join(", ", selectedSoftSkills);
        System.out.println("Soft Skills: " + softSkills);

        insertFormData(level, hardSkills, softSkills);

        // Envoyer les données au serveur Flask pour obtenir des recommandations
        sendFormDataToServer(level, hardSkills, softSkills);
        //showInformation("Form submitted successfully!");
    }

    private boolean isValidHardSkillsFormat(String hardSkills) {
        return hardSkills.matches("[a-zA-Z0-9éèà-ô,\\s]+");
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showInformation(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void insertFormData(String level, String hardSkills, String softSkills) {
        try (Connection con = ConnectionDB.getConnection();
             PreparedStatement st = con.prepareStatement("INSERT INTO user_data (user_id, level, hard_skills, soft_skills) VALUES (?, ?, ?, ?)")) {
            st.setInt(1, userId);
            st.setString(2, level);
            st.setString(3, hardSkills);
            st.setString(4, softSkills);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while inserting data into the database.");
        }
    }

    // Setter for userId
    public void setUserId(int userId) {
        this.userId = userId;
    }
}
