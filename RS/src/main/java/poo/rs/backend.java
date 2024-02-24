package poo.rs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class backend extends Application {
    public static void main(String[] args) {
        // Launch the JavaFX application
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // User data
            String userHardSkills = "HTML,CSS,javascript,php,nodejs,expressjs,prisma orm,sql,bootstrap,c,c++,java,ms office 365,visualisation,SE";
            String userSoftSkills = "leadership";

            // Prepare user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("hard_skills", userHardSkills);
            userData.put("soft_skills", userSoftSkills);

            // Convert user data to JSON
            String jsonData = convertMapToJson(userData);

            // API endpoint
            String apiEndpoint = "http://localhost:5000/recommend_jobs";

            // Send POST request to Flask API
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

            // Display the visualization in JavaFX
            Platform.runLater(() -> showJavaFXChart(primaryStage, response.body()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Convert a Map to JSON string
    private static String convertMapToJson(Map<String, Object> data) {
        StringBuilder jsonData = new StringBuilder("{");
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            jsonData.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
        }
        jsonData.deleteCharAt(jsonData.length() - 1);  // Remove the trailing comma
        jsonData.append("}");
        return jsonData.toString();
    }
    // Display the JavaFX bar chart
    private void showJavaFXChart(Stage primaryStage, String responseBody) {
        // Création du graphique à barres avec des barres horizontales
        BarChart<Number, String> barChart = new BarChart<>(new NumberAxis(), new CategoryAxis());
        barChart.setTitle("Job Recommendations");
        // Analyse de la réponse JSON et ajout des données au graphique
        Map<String, Double> jobRecommendations = parseJsonResponse(responseBody);
        XYChart.Series<Number, String> series = new XYChart.Series<>();
        for (Map.Entry<String, Double> entry : jobRecommendations.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getValue(), entry.getKey()));
        }
        // Ajout de la série au graphique
        barChart.getData().add(series);
        // Personnalisation des couleurs des barres avec CSS
        for (XYChart.Data<Number, String> data : series.getData()) {
            if (data.getXValue().doubleValue() > 60.0) {
                data.getNode().setStyle("-fx-bar-fill: #4CAF50;"); // Vert pour les pourcentages supérieurs à 30
            } else {
                data.getNode().setStyle("-fx-bar-fill: #2196F3;"); // Bleu pour les pourcentages inférieurs ou égaux à 30
            }
        }
        // Création d'une scène et configuration de la scène dans la fenêtre principale
        Scene scene = new Scene(barChart, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Job Recommendations Chart");
        primaryStage.show();
    }
    // Parse JSON response and return a map of job titles and percentages
    private Map<String, Double> parseJsonResponse(String responseBody) {
        try {
            // Use Jackson library to parse JSON
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
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>(); // Handle parsing errors gracefully
        }
    }
}