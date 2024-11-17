package PersonalizedNews;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class AddArticles {
    @FXML
    public TextField articleName;
    @FXML
    public TextField articleAuthor;
    @FXML
    public DatePicker publishedDate;
    @FXML
    public TextField articleID;
    @FXML
    public CheckBox checkAI;
    @FXML
    public CheckBox checkTech;
    @FXML
    public CheckBox checkHealth;
    @FXML
    public CheckBox checkSports;
    @FXML
    public CheckBox checkBusiness;
    @FXML
    public CheckBox checkEntertainment;
    @FXML
    public CheckBox checkTravel;
    @FXML
    public CheckBox checkPolitics;

    public void onClickAdd(ActionEvent event) {
    }
}
