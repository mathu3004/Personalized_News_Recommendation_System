package PersonalizedNews;

import com.mongodb.client.*;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.bson.Document;

import java.util.*;

public class FetchArticles {
    // Connect to MongoDB
    public static MongoCollection<Document> connectToDatabase(String dbName, String collectionName) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("News");
        return database.getCollection("Articles");
    }

    // Fetch articles from MongoDB
    public static List<String> fetchArticles(MongoCollection<Document> collection) {
        List<String> articles = new ArrayList<>();
        FindIterable<Document> documents = collection.find();

        for (Document doc : documents) {
            articles.add(doc.getString("content")); // Assuming "content" holds the article text
        }

        return articles;
    }

    // Tokenize the article text
    public static String[] tokenizeText(String text) {
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        return tokenizer.tokenize(text);
    }

    // Remove stopwords
    public static List<String> removeStopwords(String[] tokens, List<String> stopwords) {
        return Arrays.stream(tokens)
                .filter(token -> !stopwords.contains(token.toLowerCase()))
                .toList();
    }

    // Calculate word frequency
    public static Map<String, Integer> calculateWordFrequency(List<String> tokens) {
        Map<String, Integer> wordFrequency = new HashMap<>();
        for (String token : tokens) {
            wordFrequency.put(token.toLowerCase(), wordFrequency.getOrDefault(token.toLowerCase(), 0) + 1);
        }
        return wordFrequency;
    }

    // Categorize article based on word frequency
    public static String categorizeArticle(Map<String, Integer> wordFrequency, Map<String, List<String>> categoryKeywords) {
        String bestCategory = "Uncategorized";
        int maxMatches = 0;

        for (Map.Entry<String, List<String>> entry : categoryKeywords.entrySet()) {
            String category = entry.getKey();
            List<String> keywords = entry.getValue();

            int matches = 0;
            for (String keyword : keywords) {
                matches += wordFrequency.getOrDefault(keyword.toLowerCase(), 0);
            }

            if (matches > maxMatches) {
                maxMatches = matches;
                bestCategory = category;
            }
        }

        return bestCategory;
    }

    public static void main(String[] args) {
        try {
            // Connect to MongoDB
            MongoCollection<Document> collection = connectToDatabase("articlesDB", "articles");

            // Fetch articles from MongoDB
            List<String> articles = fetchArticles(collection);

            // Define stopwords
            List<String> stopwords = Arrays.asList("the", "is", "in", "at", "of", "on", "and", "a", "to", "with");

            // Define category keywords
            Map<String, List<String>> categoryKeywords = new HashMap<>();
            categoryKeywords.put("Technology", Arrays.asList("AI", "technology", "computing", "software", "hardware"));
            categoryKeywords.put("Health", Arrays.asList("health", "wellness", "medicine", "doctor", "fitness"));
            categoryKeywords.put("Sports", Arrays.asList("sports", "game", "athlete", "fitness", "competition"));
            categoryKeywords.put("AI", Arrays.asList("artificial", "intelligence", "machine", "learning", "deep"));

            // Process and categorize each article
            for (String article : articles) {
                // Tokenize the article
                String[] tokens = tokenizeText(article);

                // Remove stopwords
                List<String> filteredTokens = removeStopwords(tokens, stopwords);

                // Calculate word frequency
                Map<String, Integer> wordFrequency = calculateWordFrequency(filteredTokens);

                // Categorize the article
                String category = categorizeArticle(wordFrequency, categoryKeywords);

                // Output the result
                System.out.println("Article: " + article);
                System.out.println("Category: " + category);
                System.out.println("-------------------------------------");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
