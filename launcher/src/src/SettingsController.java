package src;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import src.domain.NewsItem;
import src.domain.Register;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static src.Main.msg;

public class SettingsController implements Initializable {

    private Register reg;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {

            reg = new Register();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onCancelClicked() {
        msg("Test");
    }

    @FXML
    private void onConfirmClicked() {
        msg("Test");
    }
}
