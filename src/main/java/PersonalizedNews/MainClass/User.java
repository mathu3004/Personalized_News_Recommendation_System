package PersonalizedNews.MainClass;

import java.time.LocalDate;
import java.util.*;

public class User extends Human {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private List<String> preferences;
    private Set<Article> likedArticles;
    private Set<Article> savedArticles;
    private Set<Article> readArticles;
    private Set<Article> skippedArticles;

    public User(String firstName, String lastName, String email, String username, String password, LocalDate dateOfBirth, String gender, List<String> preferences) {
        super(username, email, password);
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.preferences = new ArrayList<>();
        this.likedArticles = new HashSet<>();
        this.savedArticles = new HashSet<>();
        this.readArticles = new HashSet<>();
        this.skippedArticles = new HashSet<>();
    }

    public User(String username, String email, String password) {
        super(username, email, password);
        this.likedArticles = new HashSet<>();
        this.savedArticles = new HashSet<>();
        this.readArticles = new HashSet<>();
        this.skippedArticles = new HashSet<>();
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

    // Methods for managing articles
    public void likeArticle(Article article) {
        likedArticles.add(article);
    }

    public void saveArticle(Article article) {
        savedArticles.add(article);
    }

    public void readArticle(Article article) {
        readArticles.add(article);
    }

    public void skipArticle(Article article) {
        skippedArticles.add(article);
    }

    public Set<Article> getLikedArticles() {
        return likedArticles;
    }

    public Set<Article> getSavedArticles() {
        return savedArticles;
    }

    public Set<Article> getReadArticles() {
        return readArticles;
    }

    public Set<Article> getSkippedArticles() {
        return skippedArticles; // Retrieve skipped articles
    }

    public void viewRatedArticles() {
        System.out.println("Articles read, liked, or saved by " + getUsername() + ":");
        for (Article article : readArticles) {
            System.out.println("Read: " + article);
        }
        for (Article article : likedArticles) {
            System.out.println("Liked: " + article);
        }
        for (Article article : savedArticles) {
            System.out.println("Saved: " + article);
        }
        for (Article article : skippedArticles) {
            System.out.println("Skipped: " + article);
        }
    }

    public void viewArticlesByCategory(Set<Article> allArticles, String category) {
        System.out.println("Viewing articles in category: " + category);
        for (Article article : allArticles) {
            if (article.getCategory().equalsIgnoreCase(category)) {
                System.out.println(article);
            }
        }
    }

    public void viewCustomizedArticles(Set<Article> allArticles) {
        System.out.println("Viewing customized articles based on preferences:");
        for (Article article : allArticles) {
            if (preferences.contains(article.getCategory())) {
                System.out.println(article);
            }
        }
    }


    @Override
    public boolean isValid() {
        return (getPassword() != null && !getPassword().trim().isEmpty() &&
                (getEmail() != null && !getEmail().trim().isEmpty() || getUsername() != null && !getUsername().trim().isEmpty()));
    }

    public String getPosition() {
        return "User";
    }

    public void viewArticlesByCategory(String category) {
        System.out.println("Viewing articles in category: " + category);
        // Logic to display articles based on category
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