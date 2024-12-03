package PersonalizedNews.MainClass;

import java.util.ArrayList;

public abstract class Human {
    private String username;
    private String email;
    private String password;

    public Human(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Abstract method to enforce implementation in subclasses
    public abstract boolean isValid();

    public abstract String getPosition();

    @Override
    public String toString() {
        return "Human{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}