package PersonalizedNews.MainClass;

import java.time.LocalDate;
import java.util.*;

public class User extends Human {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private List<String> preferences;

    // New fields to store user preferences
    private Map<String, Map<String, Integer>> ratedCategory; // category -> (articleId -> rating)
    private Set<String> preferredCategory; // High-rated categories


    public User(String firstName, String lastName, String email, String username, String password, LocalDate dateOfBirth, String gender, List<String> preferences) {
        super(username, email, password);
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.preferences = preferences;
        this.ratedCategory = new HashMap<>();
        this.preferredCategory = new HashSet<>();

    }

    public User(String username, String email, String password) {
        super(username, email, password);
        this.ratedCategory = new HashMap<>();
        this.preferredCategory = new HashSet<>();
    }

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }

    @Override
    public boolean isValid() {
        return (getPassword() != null && !getPassword().trim().isEmpty() &&
                (getEmail() != null && !getEmail().trim().isEmpty() || getUsername() != null && !getUsername().trim().isEmpty()));
    }

    public String getPosition() {
        return "User";
    }

    // Add a rating for an article
    public void ratingUpdate(String articleId, String category, int rating) {
        ratedCategory
                .computeIfAbsent(category, k -> new HashMap<>())
                .put(articleId, rating);

        // Update preferred categories if rating is high
        if (rating >= 4) {
            preferredCategory.add(category);
        } else if (rating <= 2) {
            preferredCategory.remove(category);
        }
    }

    // Get ratings for a specific category
    public Map<String, Integer> getRating(String category) {
        return ratedCategory.getOrDefault(category, new HashMap<>());
    }

    // Get all preferred categories
    public Set<String> getPreferredCategory() {
        return preferredCategory;
    }

    // Determine if the user is new
    public boolean newUserConfirmation() {
        return ratedCategory.isEmpty();
    }

    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", gender='" + gender + '\'' +
                ", preferences=" + preferences +
                ", username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", password='" + getPassword() + '\'' +
                '}';
    }
}