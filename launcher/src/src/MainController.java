package src;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import src.domain.NewsItem;
import src.domain.Register;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static src.Main.msg;

public class MainController implements Initializable {

    @FXML
    private ListView<NewsItem> listview = new ListView<>();

    private Register reg;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {

            reg = new Register();
            listview.setFocusTraversable(false);
            showNews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showNews() throws Exception {
        listview.setItems(FXCollections.observableArrayList(reg.getNewsItems()));
    }

    @FXML
    private void onSettingsClicked() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("settings.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
