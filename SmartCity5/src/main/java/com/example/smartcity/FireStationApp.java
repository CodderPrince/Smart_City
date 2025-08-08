package com.example.smartcity;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FireStationApp extends Application {

    private static final double USER_LAT = 25.7439;
    private static final double USER_LON = 89.2510;
    private static final String JSON_FILE = "rangpur_firestations.json";
    private List<FireStation> fireStations;
    private VBox fireStationLayout;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Fire Station Finder");

        Button findStationButton = createStyledButton("Find Nearby Fire Station", Color.web("#2196F3"), Color.web("#1565C0"));

        fireStationLayout = new VBox(10); // Initialize fireStationLayout
        fireStationLayout.setPadding(new Insets(20));
        fireStationLayout.setAlignment(Pos.TOP_CENTER);


        // Main layout
        VBox mainLayout = new VBox(20, findStationButton, fireStationLayout);
        mainLayout.setBackground(new Background(new BackgroundFill(Color.web("#2c3e50"), CornerRadii.EMPTY, null))); // Dark background
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);



        // Load fire station data during start, only when first time button is clicked
        try {
            fireStations = loadFireStationsFromJson();
        } catch (Exception e) {
            Label errorLabel = new Label("Error loading fire station data: " + e.getMessage());
            errorLabel.setTextFill(Color.RED);
            VBox errorLayout = new VBox(errorLabel);
            errorLayout.setAlignment(Pos.CENTER);
            errorLayout.setPadding(new Insets(20));
            Scene errorScene = new Scene(errorLayout, 400, 200);
            primaryStage.setScene(errorScene);
            primaryStage.show();
            return;
        }



        // Button action to show fire stations
        findStationButton.setOnAction(e -> {
            fireStationLayout.getChildren().clear(); // Clear previous results
            VBox fireStationDisplay = createFireStationDisplay(); // Get the result after button click
            fireStationLayout.getChildren().add(fireStationDisplay);
        });



        Scene scene = new Scene(mainLayout, 600, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private List<FireStation> loadFireStationsFromJson() throws Exception {
        List<FireStation> stations = new ArrayList<>();
        InputStream inputStream = getClass().getResourceAsStream("/" + JSON_FILE);
        if (inputStream == null) {
            throw new Exception("Could not find JSON file: " + JSON_FILE);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            JSONArray jsonArray = new JSONArray(jsonContent.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject stationJson = jsonArray.getJSONObject(i);
                double latitude = stationJson.getDouble("latitude");
                double longitude = stationJson.getDouble("longitude");
                String phoneNumber = stationJson.getString("phoneNumber");
                String name = stationJson.getString("name");

                stations.add(new FireStation(latitude, longitude, phoneNumber, name));
            }
        }
        return stations;
    }


    private VBox createFireStationDisplay() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        if (fireStations.isEmpty()) {
            Label noStationLabel = new Label("No Fire Station Found");
            noStationLabel.setFont(Font.font("Arial", 16));
            noStationLabel.setTextFill(Color.WHITE);
            layout.getChildren().add(noStationLabel);
            return layout;
        }

        for (int i = 0; i < fireStations.size(); i++) {
            FireStation station = fireStations.get(i);
            double distance = calculateDistance(USER_LAT, USER_LON, station.latitude, station.longitude);

            VBox stationBox = new VBox(5);
            stationBox.setPadding(new Insets(10));


            Color fillColor = Color.rgb((i * 70) % 255, (i * 100) % 255, (i * 130) % 255, 0.4);
            stationBox.setBackground(new Background(new BackgroundFill(fillColor, new CornerRadii(5), Insets.EMPTY)));

            Label nameLabel = new Label("Name: " + station.name);
            nameLabel.setFont(Font.font("Arial", 14));
            nameLabel.setTextFill(Color.WHITE);  // Light font color

            Label locationLabel = new Label("Location (Lat, Lon): " + station.latitude + ", " + station.longitude);
            locationLabel.setFont(Font.font("Arial", 14));
            locationLabel.setTextFill(Color.WHITE);  // Light font color


            Label phoneLabel = new Label("Phone Number: " + station.phoneNumber);
            phoneLabel.setFont(Font.font("Arial", 14));
            phoneLabel.setTextFill(Color.WHITE);  // Light font color

            Label distanceLabel = new Label("Distance: " + String.format("%.2f", distance) + " km");
            distanceLabel.setFont(Font.font("Arial", 14));
            distanceLabel.setTextFill(Color.WHITE);  // Light font color

            stationBox.getChildren().addAll(nameLabel, locationLabel, phoneLabel, distanceLabel);

            //Add fade in animation
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), stationBox);
            fadeTransition.setFromValue(0.0);
            fadeTransition.setToValue(1.0);
            fadeTransition.play();

            //Add scale in animation
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(500), stationBox);
            scaleTransition.setFromX(0.95);
            scaleTransition.setFromY(0.95);
            scaleTransition.setToX(1);
            scaleTransition.setToY(1);
            scaleTransition.play();


            layout.getChildren().add(stationBox);
        }

        return layout;
    }



    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    private Button createStyledButton(String text, Color startColor, Color endColor) {
        Button button = new Button(text);
        button.setPrefWidth(450);
        button.setPrefHeight(60);
        button.setFont(Font.font("Arial Rounded MT Bold", 25));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: linear-gradient(" + startColor.toString().replace("0x", "#") + ", " + endColor.toString().replace("0x", "#") + ");" +
                "-fx-background-radius: 15;" +
                "-fx-border-radius: 15;" +
                "-fx-border-color: white; -fx-border-width: 2;");
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setColor(Color.GRAY);
        button.setEffect(shadow);

        button.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), button);
            scaleUp.setToX(1.2);
            scaleUp.setToY(1.2);
            scaleUp.play();
        });
        button.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), button);
            scaleDown.setToX(1);
            scaleDown.setToY(1);
            scaleDown.play();
        });

        return button;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class FireStation {
    double latitude;
    double longitude;
    String phoneNumber;
    String name;

    public FireStation(double latitude, double longitude, String phoneNumber, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.phoneNumber = phoneNumber;
        this.name = name;
    }
}