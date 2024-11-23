module com.example.colorquantization {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;


    opens com.example.colorquantization to javafx.fxml;
    exports com.example.colorquantization;
}