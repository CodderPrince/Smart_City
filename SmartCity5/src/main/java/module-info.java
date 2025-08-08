module com.example.smartcity {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;

    opens com.example.smartcity to javafx.fxml;

    exports com.example.smartcity;
}