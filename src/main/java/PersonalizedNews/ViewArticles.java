package PersonalizedNews;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ViewArticles {
    @FXML
    public TableColumn columnCategory;
    @FXML
    public TableColumn columnAuthor;
    @FXML
    public TableColumn columnPublishedDate;
    @FXML
    public TableColumn columnTitle;
    @FXML
    public TableColumn columnDescription;
    @FXML
    public TableColumn columnSelect;
    @FXML
    public TableView articlesTable;

    public void onClickRead(ActionEvent event) {
    }

    public void onClickSkip(ActionEvent event) {
    }
}
