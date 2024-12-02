package PersonalizedNews.MainClass;

import org.bson.Document;

public class Admin extends Human {
    private String adminPosition;

    // Constructor
    public Admin(String username, String email, String password) {
        super(username, email, password);
    }

    public Admin(String username, String email, String password, String adminPosition) {
        super(username, email, password);
        this.adminPosition = adminPosition;
    }

    public String getAdminPosition() {
        return adminPosition;
    }

    public void setAdminPosition(String adminPosition) {
        this.adminPosition = adminPosition;
    }

    // Validate the admin details
    @Override
    public boolean isValid() {
        return (getPassword() != null && !getPassword().trim().isEmpty() &&
                (getEmail() != null && !getEmail().trim().isEmpty() || getUsername() != null && !getUsername().trim().isEmpty()));
    }

    public String getPosition() {
        return "Admin";
    }

    // Convert to MongoDB query document
    public Document toQueryDocument() {
        Document query = new Document();
        if (getEmail() != null && !getEmail().isEmpty()) {
            query.append("email", getEmail());
        }
        if (getUsername() != null && !getUsername().isEmpty()) {
            query.append("username", getUsername());
        }
        query.append("password", getPassword());
        return query;
    }

    public void manageArticles() {
        System.out.println("Managing articles..");
    }

    @Override
    public String toString() {
        return "Admin{" +
                "username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", password='" + getPassword() + '\'' +
                '}';
    }
}