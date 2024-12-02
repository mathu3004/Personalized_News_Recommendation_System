package PersonalizedNews;

import PersonalizedNews.MainClass.User;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final ExecutorService executorService = Executors.newCachedThreadPool(); // Thread pool for concurrency

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
        executorService.execute(() -> {
            if (!validateFirstName() || !validateLastName() || !validateEmail() || !validatePassword() ||
                    !validateCheckboxSelection() || !validateDateOfBirth() || !validateGenderSelection() || !validateUsername()) {
                return;
            }
            if (!validateMaxThreeCategories()) {
                showAlert("You can select a maximum of 3 preferences.", Alert.AlertType.ERROR);
                return;
            }

            // Create a User object
            User newUser  = new User(
                    firstName.getText(),
                    lastName.getText(),
                    email.getText(),
                    username.getText(),
                    password.getText(),
                    dOB.getValue(),
                    genderGroup.getSelectedToggle() == radioMale ? "Male" : "Female",
                    getSelectedPreferences()
            );

            try (MongoClient mongoClient = MongoClients.create("mongodb+srv://mathu0404:Janu3004%40@cluster0.6dlta.mongodb.net/")) {
                MongoDatabase database = mongoClient.getDatabase("News");
                MongoCollection<Document> collection = database.getCollection("UserAccounts");

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
                Document userDoc = new Document("firstName", newUser .getFirstName())
                        .append("lastName", newUser .getLastName())
                        .append("email", newUser .getEmail())
                        .append("username", newUser .getUsername())
                        .append("password", newUser .getPassword()) // Remember to encrypt passwords in production!
                        .append("dateOfBirth", newUser .getDateOfBirth().toString())
                        .append("gender", newUser .getGender())
                        .append("preferences", newUser .getPreferences());

                // Insert into MongoDB
                collection.insertOne(userDoc);

                showAlert("Account created successfully!", Alert.AlertType.INFORMATION);
                javafx.application.Platform.runLater(() -> {
                    try {
                        onClickLogin(event);
                        clearFields(); // Reset the form
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                showAlert("Failed to save data to the database.", Alert.AlertType.ERROR);
            }
        });
    }

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

    private boolean validateFirstName() {
        if (firstName.getText().matches("[a-zA-Z]+")) {
            return true;
        } else {
            showAlert("First Name must contain only alphabetic characters.", Alert.AlertType.ERROR);
            return false;
        }
    }

    private boolean validateLastName() {
        if (lastName.getText().matches("[a-zA-Z]+")) {
            return true;
        } else {
            showAlert("Last Name must contain only alphabetic characters.", Alert.AlertType.ERROR);
            return false;
        }
    }

    private boolean validateEmail() {
        String emailPattern = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$";
        if (email.getText().matches(emailPattern)) {
            return true;
        } else {
            showAlert("Please enter a valid email address.", Alert.AlertType.ERROR);
            return false;
        }
    }

    private boolean validateGenderSelection() {
        if (genderGroup.getSelectedToggle() != null) {
            return true;
        } else {
            showAlert("Please select either Male or Female.", Alert.AlertType.ERROR);
            return false;
        }
    }

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

    private boolean validateUsername() {
        String usernamePattern = "^[a-zA-Z0-9]+$";
        if (username.getText().matches(usernamePattern)) {
            return true;
        } else {
            showAlert("Username must contain only alphanumeric characters (letters and numbers).", Alert.AlertType.ERROR);
            return false;
        }
    }

    private boolean validateCheckboxSelection() {
        return checkAI.isSelected() || checkTech.isSelected() || checkSports.isSelected() || checkHealth.isSelected() || checkTravel.isSelected()
                || checkBusiness.isSelected() || checkPolitics.isSelected() || checkEntertainment.isSelected();
    }

    private boolean validateDateOfBirth() {
        LocalDate selectedDate = dOB.getValue();
        LocalDate minDate = LocalDate.now().minusYears(5); // Minimum age of 5 years

        return selectedDate != null && selectedDate.isBefore(minDate);
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setContentText(message);
            alert.show();
        });
    }

    private void clearFields() {
        javafx.application.Platform.runLater(() -> {
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
        });
    }

    @FXML
    public void onViewPassword(ActionEvent event) {
        if (password.isVisible()) {
            password.setVisible(false);
            viewPassword.setVisible(true);
            viewPassword.setManaged(true);
        } else {
            password.setVisible(true);
            viewPassword.setVisible(false);
            viewPassword.setManaged(false);
        }
    }

    @FXML
    public void onViewConfirm(ActionEvent event) {
        if (confirmPassword.isVisible()) {
            confirmPassword.setVisible(false);
            viewConfirm.setVisible(true);
            viewConfirm.setManaged(true);
        } else {
            confirmPassword.setVisible(true);
            viewConfirm.setVisible(false);
            viewConfirm.setManaged(false);
        }
    }

    @FXML
    public void onClickLogin(ActionEvent event) {
        executorService.execute(() -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("UserLogin.fxml"));
                javafx.application.Platform.runLater(() -> {
                    try {
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root, 440, 280));
                        root.getStylesheets().add(getClass().getResource("Personalized_News.css").toExternalForm());
                        stage.setTitle("User Login");
                        stage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void onClickReset(ActionEvent event) {
        clearFields();
    }
}