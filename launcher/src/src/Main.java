package src;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import src.domain.NewsItem;
import src.domain.Register;
import src.domain.constants.ApplicationConstants;
import src.domain.constants.FileConstants;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {

            primaryStage.setResizable(false);

            Parent root = FXMLLoader.load(getClass().getResource(FileConstants.FXML_FILE_PATH));

            primaryStage.setTitle(ApplicationConstants.TITLE);
            primaryStage.setScene(new Scene(root));

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void msg(String msg) {
        System.out.println(msg);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
