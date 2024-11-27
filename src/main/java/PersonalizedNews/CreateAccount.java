package PersonalizedNews;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    @FXML
    public TextField username;
    @FXML
    public CheckBox checkBusiness;
    @FXML
    public CheckBox checkPolitics;
    @FXML
    public CheckBox checkEntertainment;
    private ToggleGroup genderGroup;

    // Initialize method to set preferences
    @FXML
    public void initialize() {
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
                !validateCheckboxSelection() || !validateDateOfBirth() || !validateGenderSelection() || !validateUsername()) {
            return; // Exit if any validation fails
        }
        if (!validateMaxThreeCategories()) {
            showAlert("You can select a maximum of 3 preferences.", Alert.AlertType.ERROR);
            return;
        }

        try {
            // Connect to MongoDB
            MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017");
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<org.bson.Document> collection = database.getCollection("UserAccounts");

            // Check for duplicate email
            Document existingEmail = collection.find(new Document("email", email.getText())).first();
            if (existingEmail != null) {
                showAlert("This email is already registered. Please use a different email.", Alert.AlertType.ERROR);
                return;
            }

            // Check for duplicate username
            Document existingUsername = collection.find(new Document("username", username.getText())).first();
            if (existingUsername != null) {
                showAlert("This username is already taken. Please choose a different username.", Alert.AlertType.ERROR);
                return;
            }

            // Prepare user data
            Document user = new Document("firstName", firstName.getText())
                    .append("lastName", lastName.getText())
                    .append("email", email.getText())
                    .append("username", username.getText())
                    .append("password", password.getText()) // Encrypt passwords in production!
                    .append("dateOfBirth", dOB.getValue().toString())
                    .append("gender", genderGroup.getSelectedToggle() == radioMale ? "Male" : "Female")
                    .append("preferences", getSelectedPreferences());

            // Insert into MongoDB
            collection.insertOne(user);

            showAlert("Account created successfully!", Alert.AlertType.INFORMATION);
            onClickLogin(event);
            clearFields(); // Reset the form
        } catch (Exception e) {
            showAlert("Failed to save data to the database.", Alert.AlertType.ERROR);
        }
    }

    // Helper method to get selected preferences
    private List<String> getSelectedPreferences() {
        List<String> preferences = new ArrayList<>();
        if (checkAI.isSelected()) preferences.add("AI");
        if (checkTech.isSelected()) preferences.add("Technology");
        if (checkSports.isSelected()) preferences.add("Sports");
        if (checkHealth.isSelected()) preferences.add("Health");
        if (checkTravel.isSelected()) preferences.add("Travel");
        if (checkBusiness.isSelected()) preferences.add("Business");
        if (checkPolitics.isSelected()) preferences.add("Politics");
        if (checkEntertainment.isSelected()) preferences.add("Entertainment");
        return preferences;
    }

    // Validation for max three categories
    private boolean validateMaxThreeCategories() {
        int count = 0;
        if (checkAI.isSelected()) count++;
        if (checkTech.isSelected()) count++;
        if (checkSports.isSelected()) count++;
        if (checkHealth.isSelected()) count++;
        if (checkTravel.isSelected()) count++;
        if (checkBusiness.isSelected()) count++;
        if (checkPolitics.isSelected()) count++;
        if (checkEntertainment.isSelected()) count++;

        return count <= 3;
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
        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (password.getText().matches(passwordPattern)) {
            if (password.getText().equals(confirmPassword.getText())) {
                return true;
            } else {
                showAlert("Passwords do not match.", Alert.AlertType.ERROR);
                return false;
            }
        } else {
            showAlert("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character.", Alert.AlertType.ERROR);
            return false;
        }
    }

    // Method to validate username
    private boolean validateUsername() {
        String usernamePattern = "^[a-zA-Z0-9]+$";
        if (username.getText().matches(usernamePattern)) {
            return true;
        } else {
            showAlert("Username must contain only alphanumeric characters (letters and numbers).", Alert.AlertType.ERROR);
            return false;
        }
    }

    // Method to validate that at least one checkbox is selected
    private boolean validateCheckboxSelection() {
        if (checkAI.isSelected() || checkTech.isSelected() || checkSports.isSelected() || checkHealth.isSelected() || checkTravel.isSelected()
                || checkBusiness.isSelected() || checkPolitics.isSelected() || checkEntertainment.isSelected()) {
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
        username.clear();
        dOB.setValue(null);
        genderGroup.selectToggle(null);
        checkAI.setSelected(false);
        checkTech.setSelected(false);
        checkSports.setSelected(false);
        checkHealth.setSelected(false);
        checkTravel.setSelected(false);
        checkEntertainment.setSelected(false);
        checkBusiness.setSelected(false);
        checkPolitics.setSelected(false);
    }

    @FXML
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

    @FXML
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

    @FXML
    public void onClickLogin(ActionEvent event) {
        try {
            // Navigate to the signup page
            Parent root = FXMLLoader.load(getClass().getResource("UserLogin.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 440, 280));
            root.getStylesheets().add(getClass().getResource("Personalized_News.css").toExternalForm());
            stage.setTitle("User Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClickReset(ActionEvent event) {
        clearFields();
    }
}