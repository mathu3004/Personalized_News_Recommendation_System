package PersonalizedNews;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class CreateAccount {

    @FXML
    public TextField firstName;
    @FXML
    public TextField lastName;
    @FXML
    public TextField email;
    @FXML
    public TextField password;
    @FXML
    public TextField confirmPassword;
    @FXML
    public ChoiceBox<String> Preferences;
    @FXML
    public TextField viewPassword;
    @FXML
    public TextField viewConfirm;
    @FXML
    public DatePicker dOB;
    @FXML
    public RadioButton radioMale;
    @FXML
    public RadioButton radioFemale;
    @FXML
    public CheckBox checkAI;
    @FXML
    public CheckBox checkTech;
    @FXML
    public CheckBox checkSports;
    @FXML
    public CheckBox checkHealth;
    @FXML
    public CheckBox checkTravel;
    private ToggleGroup genderGroup;

    // Initialize method to set preferences
    @FXML
    public void initialize() {
        Preferences.getItems().addAll("AI", "Technology", "Sports", "Health", "Travel");
        viewPassword.textProperty().bindBidirectional(password.textProperty());
        viewConfirm.textProperty().bindBidirectional(confirmPassword.textProperty());

        viewConfirm.setManaged(false);
        viewConfirm.setVisible(false);
        viewPassword.setManaged(false);
        viewPassword.setVisible(false);

        // Group the radio buttons
        genderGroup = new ToggleGroup();
        radioMale.setToggleGroup(genderGroup);
        radioFemale.setToggleGroup(genderGroup);
    }

    @FXML
    public void onSubmit(ActionEvent event) {
        if (!validateFirstName() || !validateLastName() || !validateEmail() || !validatePassword() ||
                !validateCheckboxSelection() || !validateDateOfBirth() || !validateGenderSelection()) {
            return; // Exit if any validation fails
        }
        // Additional account creation logic goes here
        showAlert("Account created successfully!", Alert.AlertType.INFORMATION);
        clearFields(); // Clear fields after successful submission}
    }
    // Method to validate first name
    private boolean validateFirstName() {
        if (firstName.getText().matches("[a-zA-Z]+")) {
            return true;
        } else {
            showAlert("First Name must contain only alphabetic characters.", Alert.AlertType.ERROR);
            return false;
        }
    }

    // Method to validate last name
    private boolean validateLastName() {
        if (lastName.getText().matches("[a-zA-Z]+")) {
            return true;
        } else {
            showAlert("Last Name must contain only alphabetic characters.", Alert.AlertType.ERROR);
            return false;
        }
    }

    // Method to validate email format
    private boolean validateEmail() {
        String emailPattern = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$";
        if (email.getText().matches(emailPattern)) {
            return true;
        } else {
            showAlert("Please enter a valid email address.", Alert.AlertType.ERROR);
            return false;
        }
    }
    // Method to validate gender selection
    private boolean validateGenderSelection() {
        if (genderGroup.getSelectedToggle() != null) {
            return true;
        } else {
            showAlert("Please select either Male or Female.", Alert.AlertType.ERROR);
            return false;
        }
    }


    // Method to validate password and confirm password
    private boolean validatePassword() {
        if (password.getText().equals(confirmPassword.getText())) {
            return true;
        } else {
            showAlert("Passwords do not match.", Alert.AlertType.ERROR);
            return false;
        }
    }

    // Method to validate that at least one checkbox is selected
    private boolean validateCheckboxSelection() {
        if (checkAI.isSelected() || checkTech.isSelected() || checkSports.isSelected() || checkHealth.isSelected() || checkTravel.isSelected()) {
            return true;
        } else {
            showAlert("Please select at least one preference.", Alert.AlertType.ERROR);
            return false;
        }
    }

    // Method to validate date of birth
    private boolean validateDateOfBirth() {
        LocalDate selectedDate = dOB.getValue();
        LocalDate minDate = LocalDate.now().minusYears(5); // Minimum age of 5 years

        if (selectedDate != null && selectedDate.isBefore(minDate)) {
            return true;
        } else {
            showAlert("Date of Birth must be at least 5 years ago.", Alert.AlertType.ERROR);
            return false;
        }
    }

    // Method to show alert messages
    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.show();
    }

    // Method to clear all fields (refresh the form)
    private void clearFields() {
        firstName.clear();
        lastName.clear();
        email.clear();
        password.clear();
        confirmPassword.clear();
        viewPassword.clear();
        viewConfirm.clear();
        Preferences.getSelectionModel().clearSelection();
        dOB.setValue(null);
        genderGroup.selectToggle(null);
        checkAI.setSelected(false);
        checkTech.setSelected(false);
        checkSports.setSelected(false);
        checkHealth.setSelected(false);
        checkTravel.setSelected(false);}

    public void onViewPassword(ActionEvent event) {
        if(password.isVisible()) {
            password.setVisible(false);
            viewPassword.setVisible(true);
            viewPassword.setManaged(true);
        } else{
            password.setVisible(true);
            viewPassword.setVisible(false);
            viewPassword.setManaged(false);
        }
    }

    public void onViewConfirm(ActionEvent event) {
        if(confirmPassword.isVisible()) {
            confirmPassword.setVisible(false);
            viewConfirm.setVisible(true);
            viewConfirm.setManaged(true);
        } else{
            confirmPassword.setVisible(true);
            viewConfirm.setVisible(false);
            viewConfirm.setManaged(false);
        }
    }
}
