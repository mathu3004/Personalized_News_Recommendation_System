package PersonalizedNews;

import com.mongodb.client.*;
import org.bson.Document;

import java.util.*;

public class ArticleCategorizer {

    // Predefined categories and their associated keywords
    private static final Map<String, List<String>> categoryKeywords = new HashMap<>();

    static {
        categoryKeywords.put("Sports", Arrays.asList("football", "cricket", "tennis", "game", "tournament"));
        categoryKeywords.put("Travel", Arrays.asList("travel", "vacation", "tour", "destination", "journey"));
        categoryKeywords.put("Health", Arrays.asList("health", "fitness", "medicine", "disease", "wellness"));
        categoryKeywords.put("Entertainment", Arrays.asList("movie", "music", "celebrity", "show", "entertainment"));
        categoryKeywords.put("Politics", Arrays.asList("election", "government", "policy", "politician", "politics"));
        categoryKeywords.put("Business", Arrays.asList("business", "economy", "market", "trade", "finance"));
        categoryKeywords.put("AI", Arrays.asList("artificial intelligence", "machine learning", "neural network", "AI", "deep learning"));
        categoryKeywords.put("Technology", Arrays.asList("technology", "software", "internet", "computer", "hardware"));
    }

    public static void main(String[] args) {
        // MongoDB connection URI
        String mongoUri = "mongodb://127.0.0.1:27017";

        // Connect to MongoDB
        try (MongoClient mongoClient = MongoClients.create(mongoUri)) {
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<Document> articlesCollection = database.getCollection("Articles");
            MongoCollection<Document> categorizedCollection = database.getCollection("CategorizedArticles");

            // Clean the CategorizedArticles collection
            System.out.println("Cleaning the CategorizedArticles collection...");
            categorizedCollection.deleteMany(new Document()); // Deletes all documents in the collection

            // Fetch uncategorized articles
            FindIterable<Document> uncategorizedArticles = articlesCollection.find(new Document("category", null));

            for (Document article : uncategorizedArticles) {
                String content = article.getString("content");
                String title = article.getString("title");
                String author = article.getString("author");
                String description = article.getString("description");
                Integer articleId = article.getInteger("articleId");

                // Handle the publishedAt field
                Object publishedAtObj = article.get("publishedAt");
                String publishedAt = null;
                if (publishedAtObj instanceof String) {
                    publishedAt = (String) publishedAtObj;
                }

                if (content != null && !content.isEmpty()) {
                    // Categorize the article
                    String category = categorizeArticle(content);

                    // Create a new document for the CategorizedArticles collection
                    Document categorizedArticle = new Document("articleId", articleId)
                            .append("title", title)
                            .append("author", author)
                            .append("description", description)
                            .append("publishedAt", publishedAt)
                            .append("content", content)
                            .append("category", category);

                    // Insert the categorized article into CategorizedArticles
                    categorizedCollection.insertOne(categorizedArticle);

                    System.out.println("Categorized article: " + title + " -> " + category);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to categorize an article
    private static String categorizeArticle(String articleText) {
        // Preprocess text
        String processedText = articleText.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");

        // Tokenize text
        String[] tokens = processedText.split("\\s+");

        // Count keyword matches for each category
        Map<String, Integer> categoryScores = new HashMap<>();
        for (String category : categoryKeywords.keySet()) {
            int score = 0;
            for (String keyword : categoryKeywords.get(category)) {
                for (String token : tokens) {
                    if (token.contains(keyword)) {
                        score++;
                    }
                }
            }
            categoryScores.put(category, score);
        }

        // Return the category with the highest score
        return categoryScores.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow(() -> new RuntimeException("No category found"))
                .getKey();
    }
}
