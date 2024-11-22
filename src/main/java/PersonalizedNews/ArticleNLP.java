package PersonalizedNews;

import com.mongodb.client.*;
import org.apache.commons.text.similarity.CosineSimilarity;
import org.bson.Document;

import java.util.*;

public class ArticleNLP {

    // Predefined categories and their example descriptions
    private static final Map<String, String> categoryExamples = new HashMap<>();

    static {
        categoryExamples.put("Sports", "Sports include football, cricket, tennis, and other games played competitively.");
        categoryExamples.put("Travel", "Travel involves journeys, tours, vacations, and exploring destinations.");
        categoryExamples.put("Health", "Health topics cover fitness, wellness, medicine, and diseases.");
        categoryExamples.put("Entertainment", "Entertainment includes movies, music, celebrity news, and shows.");
        categoryExamples.put("Politics", "Politics involves elections, government policies, and political events.");
        categoryExamples.put("Business", "Business topics include finance, economy, markets, and trade.");
        categoryExamples.put("AI", "AI includes artificial intelligence, machine learning, neural networks, and deep learning.");
        categoryExamples.put("Technology", "Technology covers software, hardware, internet, and computer-related advancements.");
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
                String content = article.getString("content"); // Assume articles have a "content" field
                String title = article.getString("title"); // Title of the article
                if (content != null && !content.isEmpty()) {
                    // Categorize the article using NLP
                    String category = categorizeArticleUsingNLP(content);

                    // Create a new document for the CategorizedArticles collection
                    Document categorizedArticle = new Document("title", title)
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

    // Method to categorize an article using TF-IDF and cosine similarity
    private static String categorizeArticleUsingNLP(String articleText) {
        CosineSimilarity cosineSimilarity = new CosineSimilarity();

        // Preprocess the article text
        String processedText = preprocessText(articleText);

        // Calculate similarity scores for each category
        Map<CharSequence, Integer> articleVector = vectorizeText(processedText);
        String bestCategory = null;
        double maxSimilarity = -1;

        for (Map.Entry<String, String> categoryEntry : categoryExamples.entrySet()) {
            String category = categoryEntry.getKey();
            String example = preprocessText(categoryEntry.getValue());
            Map<CharSequence, Integer> exampleVector = vectorizeText(example);

            double similarity = cosineSimilarity.cosineSimilarity(articleVector, exampleVector);

            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestCategory = category;
            }
        }

        return bestCategory != null ? bestCategory : "Uncategorized";
    }

    // Helper method to preprocess text (lowercase, remove punctuation, etc.)
    private static String preprocessText(String text) {
        return text.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").trim();
    }

    // Helper method to vectorize text for cosine similarity
    private static Map<CharSequence, Integer> vectorizeText(String text) {
        Map<CharSequence, Integer> vector = new HashMap<>();
        String[] tokens = text.split("\\s+");

        for (String token : tokens) {
            vector.put(token, vector.getOrDefault(token, 0) + 1);
        }

        return vector;
    }
}
