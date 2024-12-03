package PersonalizedNews.UserMaintainance;

import PersonalizedNews.MainClass.User;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.bson.Document;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class ManageProfile {
    @FXML
    public TextField email;
    @FXML
    public RadioButton radioFemale;
    @FXML
    public RadioButton radioMale;
    @FXML
    public TextField fName;
    @FXML
    public TextField lName;
    @FXML
    public PasswordField newPassword;
    @FXML
    public PasswordField confirmNewPassword;
    @FXML
    public TextField viewNewPassword;
    @FXML
    public TextField viewNewConfirmPassword;
    @FXML
    public CheckBox checkAI;
    @FXML
    public CheckBox checkTech;
    @FXML
    public CheckBox checkHealth;
    @FXML
    public CheckBox checkSports;
    @FXML
    public CheckBox checkTravel;
    @FXML
    public DatePicker dOB;
    @FXML
    public CheckBox checkEntertainment;
    @FXML
    public CheckBox checkPolitics;
    @FXML
    public CheckBox checkBusiness;
    @FXML
    public TextField username;
    @FXML
    public TextField currentPasswordText;
    @FXML
    public PasswordField currentPasswordField;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private boolean isCurrentPasswordVisible = false;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    private final ExecutorService executorService = Executors.newCachedThreadPool(); // Thread pool for concurrency

    @FXML
    public void initialize() {
        viewNewPassword.setVisible(false);
        viewNewConfirmPassword.setVisible(false);
        currentPasswordText.setVisible(false);
    }

    @FXML
    public void onClickCheck() {
        executorService.execute(this::loadUserDetails);
    }

    private void loadUserDetails() {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017/")) {
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<Document> collection = database.getCollection("UserAccounts");

            String enteredEmail = email.getText().trim();
            String enteredUsername = username.getText().trim();
            String enteredCurrentPassword = isCurrentPasswordVisible ? currentPasswordText.getText().trim() : currentPasswordField.getText().trim();

            // Validation for username/email and current password
            if (enteredEmail.isEmpty() && enteredUsername.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter either username or email!");
                return;
            }
            if (enteredCurrentPassword.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter your current password!");
                return;
            }

            Document query = new Document();
            if (!enteredEmail.isEmpty()) query.append("email", enteredEmail);
            if (!enteredUsername.isEmpty()) query.append("username", enteredUsername);
            query.append("password", enteredCurrentPassword);

            Document userDocument = collection.find(query).first();

            if (userDocument != null) {
                User user = new User(
                        userDocument.getString("firstName"),
                        userDocument.getString("lastName"),
                        userDocument.getString("email"),
                        userDocument.getString("username"),
                        userDocument.getString("password"),
                        LocalDate.parse(userDocument.getString("dateOfBirth")),
                        userDocument.getString("gender"),
                        userDocument.getList("preferences", String.class)
                );

                Platform.runLater(() -> populateFields(user));

                showAlert(Alert.AlertType.INFORMATION, "Load Success", "User details loaded successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "User Not Found", "No user found with the provided details!");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void populateFields(User user) {
        fName.setText(user.getFirstName());
        lName.setText(user.getLastName());
        email.setText(user.getEmail());
        username.setText(user.getUsername());
        dOB.setValue(user.getDateOfBirth());

        if ("Female".equalsIgnoreCase(user.getGender())) {
            radioFemale.setSelected(true);
        } else if ("Male".equalsIgnoreCase(user.getGender())) {
            radioMale.setSelected(true);
        }

        setPreferences(user.getPreferences());

        newPassword.setText(user.getPassword());
        confirmNewPassword.setText(user.getPassword());
        viewNewPassword.setText(user.getPassword());
        viewNewConfirmPassword.setText(user.getPassword());
    }

    @FXML
    public void onClickUpdate() {
        executorService.execute(this::updateProfile);
    }

    private void updateProfile() {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017/")) {
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<Document> collection = database.getCollection("UserAccounts");

            // Validate inputs
            if (!validateInputs()) return;

            User updatedUser = new User(
                    fName.getText().trim(),
                    lName.getText().trim(),
                    email.getText().trim(),
                    username.getText().trim(),
                    newPassword.getText().trim(),
                    dOB.getValue(),
                    radioMale.isSelected() ? "Male" : "Female",
                    getSelectedPreferences()
            );

            Document query = new Document("email", email.getText().trim());
            Document update = new Document("$set", mapUserToDocument(updatedUser));

            collection.updateOne(query, update);
            clearAllFields();
            showAlert(Alert.AlertType.INFORMATION, "Update Success", "Profile updated successfully!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    private Document mapUserToDocument(User user) {
        return new Document()
                .append("firstName", user.getFirstName())
                .append("lastName", user.getLastName())
                .append("email", user.getEmail())
                .append("username", user.getUsername())
                .append("password", user.getPassword())
                .append("dateOfBirth", user.getDateOfBirth().toString())
                .append("gender", user.getGender())
                .append("preferences", user.getPreferences());
    }

    @FXML
    public void onClickCancel() {
        executorService.execute(this::clearAllFields);
    }

    private void clearAllFields() {
        Platform.runLater(() -> {
            email.clear();
            username.clear();
            fName.clear();
            lName.clear();
            currentPasswordField.clear();
            currentPasswordText.clear();
            newPassword.clear();
            confirmNewPassword.clear();
            viewNewPassword.clear();
            viewNewConfirmPassword.clear();
            radioFemale.setSelected(false);
            radioMale.setSelected(false);
            dOB.setValue(null);
            clearPreferences();
        });
    }

    @FXML
    public void onViewPassword() {
        togglePasswordField(newPassword, viewNewPassword, isPasswordVisible);
        isPasswordVisible = !isPasswordVisible;
    }

    @FXML
    public void onViewConfirm() {
        togglePasswordField(confirmNewPassword, viewNewConfirmPassword, isConfirmPasswordVisible);
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
    }

    @FXML
    public void onClickViewCurrentPassword() {
        togglePasswordField(currentPasswordField, currentPasswordText, isCurrentPasswordVisible);
        isCurrentPasswordVisible = !isCurrentPasswordVisible;
    }

    private boolean validateInputs() {
        if (fName.getText().trim().isEmpty() || !fName.getText().matches("[A-Za-z]+")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "First name must be alphabetical!");
            return false;
        }
        if (lName.getText().trim().isEmpty() || !lName.getText().matches("[A-Za-z]+")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Last name must be alphabetical!");
            return false;
        }
        if (dOB.getValue() == null || Period.between(dOB.getValue(), LocalDate.now()).getYears() < 5) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Date of birth must be greater than 5 years old!");
            return false;
        }
        if (!PASSWORD_PATTERN.matcher(newPassword.getText().trim()).matches()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Password must have at least 8 characters, including uppercase, lowercase, number, and symbol!");
            return false;
        }
        if (!newPassword.getText().equals(confirmNewPassword.getText())) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Passwords do not match!");
            return false;
        }
        if (getSelectedPreferences().size() > 3) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "You can select up to 3 preferences!");
            return false;
        }
        if (!radioMale.isSelected() && !radioFemale.isSelected()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a gender!");
            return false;
        }
        return true;
    }

    private void togglePasswordField(PasswordField passwordField, TextField textField, boolean isVisible) {
        if (isVisible) {
            passwordField.setText(textField.getText());
            passwordField.setVisible(true);
            textField.setVisible(false);
        } else {
            textField.setText(passwordField.getText());
            textField.setVisible(true);
            passwordField.setVisible(false);
        }
    }

    private void setPreferences(List<String> preferences) {
        clearPreferences();
        for (String preference : preferences) {
            switch (preference) {
                case "AI" -> checkAI.setSelected(true);
                case "Technology" -> checkTech.setSelected(true);
                case "Health" -> checkHealth.setSelected(true);
                case "Sports" -> checkSports.setSelected(true);
                case "Travel" -> checkTravel.setSelected(true);
                case "Entertainment" -> checkEntertainment.setSelected(true);
                case "Politics" -> checkPolitics.setSelected(true);
                case "Business" -> checkBusiness.setSelected(true);
            }
        }
    }

    private void clearPreferences() {
        checkAI.setSelected(false);
        checkTech.setSelected(false);
        checkHealth.setSelected(false);
        checkSports.setSelected(false);
        checkTravel.setSelected(false);
        checkEntertainment.setSelected(false);
        checkPolitics.setSelected(false);
        checkBusiness.setSelected(false);
    }

    private List<String> getSelectedPreferences() {
        List<String> preferences = new ArrayList<>();
        if (checkAI.isSelected()) preferences.add("AI");
        if (checkTech.isSelected()) preferences.add("Technology");
        if (checkHealth.isSelected()) preferences.add("Health");
        if (checkSports.isSelected()) preferences.add("Sports");
        if (checkTravel.isSelected()) preferences.add("Travel");
        if (checkEntertainment.isSelected()) preferences.add("Entertainment");
        if (checkPolitics.isSelected()) preferences.add("Politics");
        if (checkBusiness.isSelected()) preferences.add("Business");
        return preferences;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }
}