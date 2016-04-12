package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.domain.Database;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("main_form.fxml"));
        primaryStage.setTitle("News Management");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void msg(String msg) {
        System.out.println(msg);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
