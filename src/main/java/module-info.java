module com.example.cleanpicfxml {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.cleanpicfxml to javafx.fxml;
    exports com.example.cleanpicfxml;
}