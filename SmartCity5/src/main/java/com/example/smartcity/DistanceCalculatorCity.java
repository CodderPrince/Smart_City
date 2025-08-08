package com.example.smartcity;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class DistanceCalculatorCity extends Application {

    private ImageView mapImageView;
    private double currentLat;
    private double currentLon;
    private Label resultLabel;
    private TextField startLocationField;
    private TextField endLocationField;
    private static final double BEGUM_ROKEYA_UNIVERSITY_LAT = 25.7629;
    private static final double BEGUM_ROKEYA_UNIVERSITY_LON = 89.2498;


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Distance Calculator (City-Based)");

        // UI Elements
        Label startLabel = new Label("Start Location (City, Country):");
        startLabel.setFont(Font.font("Verdana", 16));
        startLabel.setTextFill(Color.web("#333333")); // Dark Gray
        startLocationField = new TextField();
        startLocationField.setFont(Font.font("Arial", 18));
        startLocationField.setPrefWidth(400);

        Label endLabel = new Label("End Location (City, Country):");
        endLabel.setFont(Font.font("Verdana", 16));
        endLabel.setTextFill(Color.web("#333333"));
        endLocationField = new TextField();
        endLocationField.setFont(Font.font("Arial", 18));
        endLocationField.setPrefWidth(400);


        Button currentLocationButton = createStyledButton("Use Rangpur as Location", Color.web("#4CAF50"), Color.web("#2E7D32"));
        Button showCurrentLocationButton = createStyledButton("Show Current Location", Color.web("#FF9800"), Color.web("#F57C00"));
        Button calculateButton = createStyledButton("Calculate Distance", Color.web("#2196F3"), Color.web("#1565C0"));
        resultLabel = new Label();
        resultLabel.setFont(Font.font("Arial", 50));
        resultLabel.setTextFill(Color.web("#000000")); // Black
        // Map Image View
        mapImageView = new ImageView();
        mapImageView.setFitWidth(500);
        mapImageView.setPreserveRatio(true);

        // Set Rangpur as default location
        setDefaultLocation("Rangpur");


        // Show current location with city name on startup
        //showCurrentLocation(); // Removed showing current location initially

        currentLocationButton.setOnAction(e -> {
            setDefaultLocation("Rangpur");
        });

        showCurrentLocationButton.setOnAction(e -> showBegumRokeyaUniversityLocation());

        // Calculate distance
        calculateButton.setOnAction(e -> {
            try {
                String startLocationText = startLocationField.getText();
                String endLocationText = endLocationField.getText();


                if (startLocationText.trim().isEmpty() || endLocationText.trim().isEmpty()) {
                    resultLabel.setText("Please fill in both location fields!");
                    return;
                }

                double[] startCoords = startLocationText.equals("Rangpur, Bangladesh") ?
                        new double[]{currentLat, currentLon} : getCoordinates(startLocationText);

                double[] endCoords = getCoordinates(endLocationText);


                if (startCoords == null || endCoords == null) {
                    resultLabel.setText("Invalid locations! Please enter valid city or country names.");
                    return;
                }

                double distance = haversine(startCoords[0], startCoords[1], endCoords[0], endCoords[1]);
                resultLabel.setText("Distance: " + String.format("%.2f", distance) + " km");
                loadMapImage(startCoords[0], startCoords[1]);
            } catch (Exception ex) {
                resultLabel.setText("Error fetching data! Please try again.");
            }
        });

        // Layout
        VBox layout = new VBox(10, startLabel, startLocationField, currentLocationButton, showCurrentLocationButton, endLabel, endLocationField,
                calculateButton, resultLabel, mapImageView);
        layout.setPadding(new Insets(20));
        layout.setSpacing(20);
        layout.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, null)));
        layout.setAlignment(Pos.CENTER);

        // Scene
        Scene scene = new Scene(layout, 600, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setDefaultLocation(String cityName) {
        try {
            double[] coords = getCoordinates(cityName);

            if (coords != null) {
                currentLat = coords[0];
                currentLon = coords[1];
                startLocationField.setText(cityName + ", Bangladesh");
                loadMapImage(currentLat, currentLon); // Optionally load map for default location
            } else {
                startLocationField.setText("Rangpur, Bangladesh (Location not found)");
                resultLabel.setText("Could not find default city's location");
            }
        } catch (Exception e) {
            resultLabel.setText("Error setting default location! Please try again.");
            startLocationField.setText("Rangpur, Bangladesh (Error)");
        }
    }


    private void showBegumRokeyaUniversityLocation() {
        currentLat = BEGUM_ROKEYA_UNIVERSITY_LAT;
        currentLon = BEGUM_ROKEYA_UNIVERSITY_LON;
        startLocationField.setText("Begum Rokeya University, Rangpur");
        loadMapImage(currentLat, currentLon);
    }



    private String getCityName(double latitude, double longitude) {
        try {

            String urlString = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=" +
                    latitude + "&lon=" + longitude + "&accept-language=en";


            HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
            Scanner scanner = new Scanner(reader);
            StringBuilder response = new StringBuilder();

            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            JSONObject json = new JSONObject(response.toString());

            if (json.has("address")) {
                JSONObject address = json.getJSONObject("address");
                if (address.has("city")){
                    return  address.getString("city");
                } else if (address.has("town")){
                    return address.getString("town");
                }else if (address.has("village")){
                    return address.getString("village");
                }else if (address.has("county")){
                    return address.getString("county");
                }
                else {
                    return null;
                }
            }
            return null;


        } catch (Exception e) {
            return null;
        }

    }
    private Button createStyledButton(String text, Color startColor, Color endColor) {
        Button button = new Button(text);
        button.setPrefWidth(400);
        button.setPrefHeight(50);
        button.setFont(Font.font("Arial Rounded MT Bold", 20));
        button.setTextFill(Color.WHITE);
        button.setStyle(String.format("-fx-background-color: linear-gradient(%s, %s);" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-color: white; -fx-border-width: 2;",
                startColor.toString().replace("0x", "#"),
                endColor.toString().replace("0x", "#")));


        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), button);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), button);
        scaleDown.setToX(1);
        scaleDown.setToY(1);

        button.setOnMouseEntered(e -> {
            scaleUp.play();
            button.setStyle(String.format("-fx-background-color: linear-gradient(%s, %s);" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-radius: 15;" +
                            "-fx-border-color: white; -fx-border-width: 2;",
                    startColor.brighter().toString().replace("0x", "#"),
                    endColor.brighter().toString().replace("0x", "#")));

        });

        button.setOnMouseExited(e -> {
            scaleDown.play();
            button.setStyle(String.format("-fx-background-color: linear-gradient(%s, %s);" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-radius: 15;" +
                            "-fx-border-color: white; -fx-border-width: 2;",
                    startColor.toString().replace("0x", "#"),
                    endColor.toString().replace("0x", "#")));
        });




        return button;
    }




    private double[] getCoordinates(String location) {
        try {
            String urlString = "https://nominatim.openstreetmap.org/search?q=" +
                    URLEncoder.encode(location, StandardCharsets.UTF_8) + "&format=json&limit=1";
            HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
            Scanner scanner = new Scanner(reader);
            StringBuilder response = new StringBuilder();

            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            JSONArray jsonArray = new JSONArray(response.toString());
            if (jsonArray.length() == 0) return null;

            JSONObject json = jsonArray.getJSONObject(0);
            return new double[]{json.getDouble("lat"), json.getDouble("lon")};
        } catch (Exception e) {
            return null;
        }
    }

    private void loadMapImage(double lat, double lon) {
        try {
            String mapUrl = "https://static-maps.yandex.ru/1.x/?lang=en-US&ll=" + lon + "," + lat + "&z=12&size=500,400";
            Image mapImage = new Image(mapUrl);
            mapImageView.setImage(mapImage);
        } catch (Exception e) {
            System.out.println("Error loading map image: " + e.getMessage());
        }
    }


    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static void main(String[] args) {
        launch(args);
    }
}