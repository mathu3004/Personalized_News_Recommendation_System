package PersonalizedNews;

import com.mongodb.client.*;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.*;

public class FetchArticlesCategory {

    private static final Map<String, List<String>> categoryKeywords = new HashMap<>();

    static {
        categoryKeywords.put("Sports", Arrays.asList("football", "sports", "cricket", "tennis", "games", "match", "tournament"));
        categoryKeywords.put("Travel", Arrays.asList("travel", "tour", "vacation", "destination", "explore", "journey"));
        categoryKeywords.put("Health", Arrays.asList("health", "fitness", "medicine", "disease", "wellness", "exercise"));
        categoryKeywords.put("Entertainment", Arrays.asList("movie", "music", "entertainment", "celebrity", "show", "concert", "film"));
        categoryKeywords.put("Politics", Arrays.asList("election", "government", "politics", "policy", "politician", "vote", "parliament"));
        categoryKeywords.put("Business", Arrays.asList("finance", "business", "economy", "market", "trade", "stock", "investment"));
        categoryKeywords.put("AI", Arrays.asList("ai", "artificial intelligence", "machine learning", "neural network", "deep learning"));
        categoryKeywords.put("Technology", Arrays.asList("software", "hardware", "internet", "computer", "technology", "gadgets"));
    }

    public static void initialize() {
        String mongoUri = "mongodb://127.0.0.1:27017";

        try (MongoClient mongoClient = MongoClients.create(mongoUri)) {
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<Document> articlesCollection = database.getCollection("Articles");
            MongoCollection<Document> categorizedCollection = database.getCollection("CategorizedArticles");

            // Clean the CategorizedArticles collection
            System.out.println("Cleaning the CategorizedArticles collection...");
            categorizedCollection.deleteMany(new Document());

            // Fetch uncategorized articles
            FindIterable<Document> uncategorizedArticles = articlesCollection.find(new Document("category", null));

            for (Document article : uncategorizedArticles) {
                String content = article.getString("content");
                String title = article.getString("title");
                String author = article.getString("author");
                String description = article.getString("description");
                Integer articleId = article.getInteger("articleId");

                Object publishedAtObj = article.get("publishedAt");
                String publishedAt = null;
                if (publishedAtObj instanceof Date) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
                    publishedAt = dateFormat.format((Date) publishedAtObj);
                } else if (publishedAtObj instanceof String) {
                    publishedAt = (String) publishedAtObj;
                }

                if (content != null && !content.isEmpty()) {
                    String category = categorizeArticleByKeywords(content);

                    Document categorizedArticle = new Document()
                            .append("articleId", articleId)
                            .append("title", title)
                            .append("author", author)
                            .append("description", description)
                            .append("publishedAt", publishedAt)
                            .append("content", content)
                            .append("category", category);

                    try {
                        categorizedCollection.insertOne(categorizedArticle);
                        System.out.println("Categorized article: " + title + " -> " + category);
                    } catch (Exception e) {
                        System.err.println("Error inserting article: " + title);
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Skipped uncategorizable article: " + title);
                }
            }
        }
    }

    private static String categorizeArticleByKeywords(String content) {
        String processedText = preprocessText(content);
        List<String> words = Arrays.asList(processedText.split("\\s+"));
        Map<String, Integer> categoryMatches = new HashMap<>();

        // Count matches for each category
        for (Map.Entry<String, List<String>> entry : categoryKeywords.entrySet()) {
            String category = entry.getKey();
            List<String> keywords = entry.getValue();
            int matchCount = (int) words.stream().filter(keywords::contains).count();
            categoryMatches.put(category, matchCount);
        }

        // Find the category with the highest match count
        String bestCategory = null;
        int maxMatchCount = 0;

        for (Map.Entry<String, Integer> entry : categoryMatches.entrySet()) {
            if (entry.getValue() > maxMatchCount) {
                maxMatchCount = entry.getValue();
                bestCategory = entry.getKey();
            }
        }

        // Handle ties or no matches
        if (maxMatchCount == 0) {
            return "Uncategorized";
        }

        return bestCategory;
    }


    static String preprocessText(String text) {
        return text.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").trim();
    }
}
