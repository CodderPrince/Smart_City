package com.example.smartcity;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONObject;
import org.json.JSONArray;

public class WeatherApp extends Application {

    private static final String API_KEY = "06b7e855452d0401cb4baf34222e5847";
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather";
    private Label cityLabel;
    private Label temperatureLabel;
    private Label humidityLabel;
    private Label windLabel;
    private Label descriptionLabel;
    private Label sunriseLabel;
    private Label sunsetLabel;
    private Label currentTimeLabel;
    private ImageView weatherIconView;
    private ImageView descriptionIconView;
    private VBox weatherContainer;


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Weather App");

        // UI Components
        Label cityLabelText = new Label("Enter City:");
        cityLabelText.setFont(Font.font("Arial Rounded MT Bold", 22));
        cityLabelText.setTextFill(Color.WHITE);

        TextField cityTextField = new TextField();
        cityTextField.setFont(Font.font("Arial Rounded MT Bold", 20));
        cityTextField.setStyle("-fx-background-color: #FFDDC1; -fx-text-fill: #333;");

        HBox inputRow = new HBox(15, cityLabelText, cityTextField);
        inputRow.setAlignment(Pos.CENTER);

        Button getWeatherButton = createStyledButton("Get Weather", "#FF6F61", "#D9534F");

        VBox inputArea = new VBox(15, inputRow, getWeatherButton);
        inputArea.setAlignment(Pos.CENTER);


        cityLabel = createAnimatedLabel("", 20, Color.CYAN);
        temperatureLabel = createAnimatedLabel("", 20, Color.YELLOW);
        humidityLabel = createAnimatedLabel("", 20, Color.GREENYELLOW);
        windLabel = createAnimatedLabel("", 20, Color.LIGHTBLUE);
        descriptionLabel = createAnimatedLabel("", 20, Color.WHITE);
        sunriseLabel = createAnimatedLabel("", 20, Color.ORANGE);
        sunsetLabel = createAnimatedLabel("", 20, Color.PINK);
        currentTimeLabel = createAnimatedLabel("", 20, Color.WHITE);

        weatherIconView = new ImageView();
        weatherIconView.setFitWidth(70);
        weatherIconView.setFitHeight(70);
        descriptionIconView = new ImageView();
        descriptionIconView.setFitWidth(100);//image size
        descriptionIconView.setFitHeight(100);

        HBox descriptionBox = new HBox(10, descriptionLabel, descriptionIconView);
        descriptionBox.setAlignment(Pos.CENTER); // Center the HBox contents

        weatherContainer = new VBox(10,
                cityLabel,
                temperatureLabel,
                humidityLabel,
                windLabel,
                descriptionBox,
                sunriseLabel,
                sunsetLabel,
                currentTimeLabel,
                weatherIconView
        );
        weatherContainer.setAlignment(Pos.CENTER);
        weatherContainer.setVisible(false);

        // Layout
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setHgap(15);
        gridPane.setVgap(15);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setBackground(new Background(new BackgroundFill(Color.DARKSLATEBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        gridPane.add(inputArea,0,0);
        gridPane.add(weatherContainer, 0, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(col1);



        // Button Action
        getWeatherButton.setOnAction(event -> {
            String city = cityTextField.getText().trim();
            if (!city.isEmpty()) {
                fetchWeatherData(city);
            } else {
                cityLabel.setText("Please enter a city.");
                cityLabel.setVisible(true);
                animateLabel(cityLabel);
            }
        });

        // Scene and Stage
        Scene scene = new Scene(gridPane, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private void fetchWeatherData(String city) {
        new Thread(() -> {
            try {
                String urlString = BASE_URL + "?q=" + city + "&appid=" + API_KEY + "&units=metric";
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());

                    javafx.application.Platform.runLater(() -> {
                        updateUI(jsonResponse);
                        weatherContainer.setVisible(true);
                    });

                } else {
                    javafx.application.Platform.runLater(() -> {
                        cityLabel.setText("Error: " + responseCode + ". Unable to fetch weather data. Please try again.");
                        cityLabel.setVisible(true);
                        animateLabel(cityLabel);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    cityLabel.setText("An error occurred: " + e.getMessage());
                    cityLabel.setVisible(true);
                    animateLabel(cityLabel);
                });

            }
        }).start();
    }


    private void updateUI(JSONObject jsonObject) {
        String cityName = jsonObject.getString("name");
        JSONObject main = jsonObject.getJSONObject("main");
        double temperature = main.getDouble("temp");
        int humidity = main.getInt("humidity");
        JSONObject wind = jsonObject.getJSONObject("wind");
        double windSpeed = wind.getDouble("speed");
        double windDirectionDegrees = wind.getDouble("deg");
        String windDirection = getWindDirection(windDirectionDegrees);
        JSONArray weatherArray = jsonObject.getJSONArray("weather");
        JSONObject weather = weatherArray.getJSONObject(0);
        String description = weather.getString("description");
        String iconCode = weather.getString("icon");
        long sunriseTimestamp = jsonObject.getJSONObject("sys").getLong("sunrise");
        long sunsetTimestamp = jsonObject.getJSONObject("sys").getLong("sunset");
        String sunriseTime = formatTime(sunriseTimestamp);
        String sunsetTime = formatTime(sunsetTimestamp);
        String currentTime = formatTime(System.currentTimeMillis() / 1000);
        Image weatherIcon = new Image("http://openweathermap.org/img/wn/" + iconCode + "@2x.png");


        cityLabel.setText("City: " + cityName);
        animateLabel(cityLabel);
        temperatureLabel.setText("Temperature: " + String.format("%.1f °C", temperature));
        animateLabel(temperatureLabel);
        humidityLabel.setText("Humidity: " + humidity + "%");
        animateLabel(humidityLabel);
        windLabel.setText("Wind: " + String.format("%.1f m/s %s", windSpeed, windDirection));
        animateLabel(windLabel);
        descriptionLabel.setText("Description: " + description);
        animateLabel(descriptionLabel);
        sunriseLabel.setText("Sunrise: " + sunriseTime);
        animateLabel(sunriseLabel);
        sunsetLabel.setText("Sunset: " + sunsetTime);
        animateLabel(sunsetLabel);
        currentTimeLabel.setText("Current Time: " + currentTime);
        animateLabel(currentTimeLabel);

        weatherIconView.setImage(weatherIcon);
        animateIcon(weatherIconView);
        Image descriptionIcon = getEmojiIcon(description);
        descriptionIconView.setImage(descriptionIcon);
        animateIcon(descriptionIconView);

    }


    private Label createAnimatedLabel(String text, double fontSize, Color color) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial Rounded MT Bold", fontSize));
        label.setTextFill(color);
        label.setVisible(false);
        return label;
    }

    private void animateLabel(Label label) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), label);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        label.setVisible(true);
        fadeIn.play();

    }

    private void animateIcon(ImageView imageView) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), imageView);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        imageView.setVisible(true);
        fadeIn.play();
    }

    private Image getEmojiIcon(String description) {
        String lowerDescription = description.toLowerCase();
        Image image = null;

        if (lowerDescription.contains("clear")) {
            image = loadImage("/images/sunglasses.png");
        } else if (lowerDescription.contains("clouds") || lowerDescription.contains("overcast")) {
            image = loadImage("/images/cloud.png");
        } else if (lowerDescription.contains("rain") || lowerDescription.contains("drizzle")) {
            image = loadImage("/images/rain.png");
        } else if (lowerDescription.contains("thunderstorm")) {
            image = loadImage("/images/thunder.png");
        } else if (lowerDescription.contains("snow")) {
            image = loadImage("/images/snow.png");
        } else if (lowerDescription.contains("mist")) {
            image = loadImage("/images/mist.png");
        }

        return image;
    }


    private Image loadImage(String resourcePath) {
        try {
            return new Image(getClass().getResourceAsStream(resourcePath));
        }
        catch (NullPointerException e) {
            System.err.println("Error loading image: " + resourcePath);
            return null;
        }
    }


    private String getWindDirection(double degrees) {
        if (degrees >= 337.5 || degrees < 22.5) {
            return "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            return "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            return "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            return "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            return "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            return "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            return "W";
        } else {
            return "NW";
        }
    }


    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        Date date = new Date(timestamp * 1000);
        return sdf.format(date);
    }

    private Button createStyledButton(String text, String startColor, String endColor) {
        Button button = new Button(text);
        button.setPrefWidth(250);
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

    public static void main(String[] args) {
        launch(args);
    }
}