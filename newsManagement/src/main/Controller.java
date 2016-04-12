package main;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import main.domain.NewsItem;
import main.domain.Register;

import java.net.URL;
import java.util.ResourceBundle;

import static main.Main.msg;

public class Controller implements Initializable {

    @FXML
    private TextField tvTitle;
    @FXML
    private TextArea taContent;
    @FXML
    private CheckBox cbImportant;

    @FXML
    private TableView<NewsItem> table = new TableView<>();

        private Register reg;

    private NewsItem selectedItem;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            reg = new Register();

            msg("initialized");

            initTable();
            showNews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTable() {
        TableColumn c1 = new TableColumn(NewsItem.NAME_ID);
        c1.setCellValueFactory(new PropertyValueFactory<NewsItem, Integer>(NewsItem.KEY_ID));

        TableColumn c2 = new TableColumn(NewsItem.NAME_TITLE);
        c2.setCellValueFactory(new PropertyValueFactory<NewsItem, String>(NewsItem.KEY_TITLE));

        TableColumn c3 = new TableColumn(NewsItem.NAME_CONTENT);
        c3.setCellValueFactory(new PropertyValueFactory<NewsItem, String>(NewsItem.KEY_CONTENT));

        TableColumn c4 = new TableColumn(NewsItem.NAME_ISIMPORTANT);
        c4.setCellValueFactory(new PropertyValueFactory<NewsItem, String>(NewsItem.KEY_ISIMPORTANT));

        TableColumn c5 = new TableColumn(NewsItem.NAME_DATE);
        c5.setCellValueFactory(new PropertyValueFactory<NewsItem, String>(NewsItem.KEY_DATE));

        table.getColumns().addAll(c1, c2, c3, c4, c5);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                onNewsItemSelected(newSelection);
            }
        });
    }

    private void onNewsItemSelected(NewsItem i) {
        selectedItem = i;
        fillControls();
    }

    private void showNews() throws Exception {
        table.setItems(FXCollections.observableArrayList(reg.getNewsItems()));
    }

    @FXML
    private void onBtnSaveClick(ActionEvent e) {
        try {
            if (selectedItem != null) {
                selectedItem.setTitle(tvTitle.getText());
                selectedItem.setImportant(cbImportant.isSelected());
                selectedItem.setContent(taContent.getText());

                if (reg.updateNewsItem(selectedItem)) {
                    showNews();
                }
            } else {
                if (!tvTitle.getText().isEmpty() && !taContent.getText().isEmpty()) {
                    String title = tvTitle.getText();
                    String content = taContent.getText();
                    boolean isimportant = false;
                    if (cbImportant.isSelected()) {
                        isimportant = true;
                    }
                    NewsItem toInsert = new NewsItem(title, content, isimportant);
                    if (reg.addNewsItem(toInsert)) {
                        selectedItem = toInsert;
                        showNews();
                    }
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @FXML
    private void onBtnCancelClick(ActionEvent e) {
        selectedItem = null;
        fillControls();
    }

    @FXML
    private void onBtnDeleteClick(ActionEvent e) {
        try {
            if (selectedItem != null) {
                if (reg.deleteNewsItem(selectedItem)) {
                    selectedItem = null;

                    showNews();
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void fillControls() {
        if (selectedItem != null) {
            tvTitle.setText(selectedItem.getTitle());
            if (selectedItem.isImportant()) {
                cbImportant.setSelected(true);
            } else {
                cbImportant.setSelected(false);
            }
            taContent.setText(selectedItem.getContent());
        } else {
            tvTitle.setText(null);
            cbImportant.setSelected(false);
            taContent.setText(null);
        }
    }
}
