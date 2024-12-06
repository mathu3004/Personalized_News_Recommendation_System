# Personalized News Platform

## Overview
The **Mark's Personalized News Platform** is a user-centric application designed to deliver tailored news content based on user preferences and interactions. The platform incorporates AI/ML-driven categorization, personalized recommendations, and robust article management tools for administrators. It enhances user engagement and provides a seamless experience for both readers and content managers.

---

## Features
### **User Features**
- **Personalized Recommendations**: News articles tailored to user preferences and interaction history.
- **User Interactions**: Like, save, skip, and read articles with a simple, intuitive interface.
- **Profile Management**: Update personal information and set category preferences.
- **Seamless Navigation**: Access categorized and recommended articles effortlessly.

### **Admin Features**
- **Article Management**: Add, edit, delete, and categorize articles.
- **Dashboard**: View all articles and manage the content effectively.

### **AI/ML-Powered Functionality**
- **Categorization**: Articles are categorized using AI-driven keyword analysis.
- **Recommendations**: Machine learning algorithms recommend articles based on user preferences and actions.

---

## Technologies Used
- **Frontend**: JavaFX for a responsive and interactive user interface.
- **Backend**: Java for business logic and MongoDB for data storage.
- **Database**: MongoDB for efficient and scalable data management.
- **AI/ML**: Keyword-based article categorization and recommendation system.

---

## Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/mathu3004/Personalized_News_Recommendation_System
   ```
2. Navigate to the project directory:
   ```bash
   cd Personalized_News_Recommendation_System
   ```
3. Set up the environment:
   - Install **Java** (version 11 or above).
   - Install **MongoDB** and ensure the server is running locally or on a cloud instance.
   - Install **JavaFX** libraries (specific steps may vary based on your IDE or build tool):
     - If using Maven, add the JavaFX dependencies to your `pom.xml`.
     - If using Gradle, configure JavaFX in `build.gradle`.

4. **Database setup**:
   - Create a MongoDB database named `News` and collections for `Articles`, `UserAccounts`, `AdminAccounts`, and `RatedArticles`.
   - Import sample data into the collections, if available (you may provide an example JSON or script).

5. **Configure the database connection**:
   - Update the connection string in the source code (e.g., `CONNECTION_STRING` in `ViewCustomArticles` and related classes):
     ```java
     private static final String CONNECTION_STRING = "mongodb+srv://mathu0404:Janu3004@cluster3004.bmusn.mongodb.net/?retryWrites=true&w=majority&appName=Cluster3004";
     ```

6. Build and run the application:
   - If using Maven:
     ```bash
     mvn clean install
     mvn exec:java
     ```
   - If using Gradle:
     ```bash
     gradle build
     gradle run
     ```
   - Alternatively, create a JAR file:
     ```bash
     java -jar Personalized_News_Recommendation_System.jar
     ```

7. Run the application:
   - Launch the app and log in as a user or admin to explore the features.

---

## Project Structure
```
Personalized-News-Platform/
|├── src/
|   |├── AdminMaintainance/
|   |├── UserMaintainance/
|   |├── Categorization/
|   |├── MainClass/
|├── resources/
|├── README.md
|├── pom.xml
```

---

## Key Functionalities
### **User Portal**
- View personalized articles and categorized articles.
- Manage profile and preferences.
- Save, like, or skip articles.

### **Admin Dashboard**
- Add, edit, and delete articles.
- View articles with categorized entries.

---

## Future Enhancements
- **Push Notifications**: Notify users of new articles in their preferred categories.
- **Analytics Dashboard**: Insights for admins on user engagement.
- **Mobile App**: Extend functionality to mobile platforms for wider accessibility.

---

## Contact
For any questions or suggestions, please contact:
- **Email**: mathusha.20233136@iit.ac.lk
- **GitHub**: [mathu3004](https://github.com/mathu3004)

---

