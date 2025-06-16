package pers.fulsun.cleanpicfxml;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class StartApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize logback
        Parent root = FXMLLoader.load(Objects.requireNonNull(StartApplication.class.getResource("/pers/fulsun/cleanpicfxml/mainpage.fxml")));
        primaryStage.setTitle("test");
        // 禁止窗口拉伸
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
