package com.example.smartcity;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SmartCity extends Application {

    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showSmartCityMainMenu();
    }

    private void showSmartCityMainMenu() {
        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setBackground(new Background(new BackgroundFill(Color.web("#2c3e50"), CornerRadii.EMPTY, null)));

        Button btnDistanceCalc = createStyledButton("Distance Calculator", "#8B4513", "#A0522D");
        btnDistanceCalc.setOnAction(e -> launchDistanceCalculator());

        Button btnFireStation = createStyledButton("Fire Station Finder", "#32cd32", "#228b22");
        btnFireStation.setOnAction(e -> launchFireStationApp());

        Button btnWeather = createStyledButton("Weather App", "#1e90ff", "#00008b");
        btnWeather.setOnAction(e -> launchWeatherApp());

        root.getChildren().addAll(btnDistanceCalc, btnFireStation, btnWeather);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Smart City Main Menu");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void launchDistanceCalculator() {
        Stage distanceCalcStage = new Stage();
        try {
            new DistanceCalculatorCity().start(distanceCalcStage);
        }catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("could not start the DistanceCalculator");
        }


    }


    private void launchFireStationApp() {
        Stage fireStationStage = new Stage();
        try {
            new FireStationApp().start(fireStationStage);
        }catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("could not start FireStationApp");
        }


    }

    private void launchWeatherApp() {
        Stage weatherStage = new Stage();
        try {
            new WeatherApp().start(weatherStage);
        }catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("could not start the WeatherApp");
        }

    }

    private Button createStyledButton(String text, String startColor, String endColor) {
        Button button = new Button(text);
        button.setPrefWidth(300);
        button.setPrefHeight(60);
        button.setFont(Font.font("Arial Rounded MT Bold", 25));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: linear-gradient(" + startColor + ", " + endColor + ");" +
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
}