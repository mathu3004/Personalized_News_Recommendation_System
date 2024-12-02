package PersonalizedNews;

import com.mongodb.client.*;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class FetchArticlesCategory {

    private static final Map<String, List<String>> categoryKeywords = new HashMap<>();

    static {
        categoryKeywords.put("Sports", Arrays.asList("football", "sports", "cricket", "tennis", "games", "match", "tournament", "esports", "athlete", "championship", "league", "play", "goal"));
        categoryKeywords.put("Travel", Arrays.asList("travel", "tour", "vacation", "destination", "explore", "journey", "adventure", "trip", "itinerary", "wanderlust", "sightseeing", "parks"));
        categoryKeywords.put("Health", Arrays.asList("health", "fitness", "medicine", "disease", "wellness", "exercise", "mental health", "diet", "nutrition", "therapy", "clinic", "hospital", "dietary"));
        categoryKeywords.put("Entertainment", Arrays.asList("movie", "music", "entertainment", "celebrity", "show", "concert", "film", "streaming", "tv", "series", "blockbuster", "animation", "movies", "screenwriting", "creativity", "documentaries"));
        categoryKeywords.put("Politics", Arrays.asList("election", "government", "politics", "political", "policy", "politician", "vote", "parliament", "congress", "legislation", "diplomacy", "senate", "campaign", "citizens", "voter", "democracy", "conflicts"));
        categoryKeywords.put("Business", Arrays.asList("finance", "business", "businesses", "economy", "market", "trade", "stock", "investment", "corporate", "startup", "profit", "revenue", "entrepreneurship", "e-commerce", "workplace", "consumers", "consumer", "trends", "competitiveness", "career", "marketing"));
        categoryKeywords.put("AI", Arrays.asList("ai", "artificial intelligence", "machine learning", "neural network", "deep learning", "algorithm", "automation", "data science", "robotics", "nlp"));
        categoryKeywords.put("Technology", Arrays.asList("software", "hardware", "internet", "cybersecurity", "computer", "technology", "gadgets", "technologies", "augmented", "reality", "vr", "quantum computing", "innovation", "cloud"));
    }

    public static void initialize() {
        String mongoUri = "mongodb+srv://mathu0404:Janu3004%40@cluster0.6dlta.mongodb.net/";

        try (MongoClient mongoClient = MongoClients.create(mongoUri)) {
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<Document> articlesCollection = database.getCollection("Articles");
            MongoCollection<Document> categorizedCollection = database.getCollection("CategorizedArticles");

            // Clean the CategorizedArticles collection
            System.out.println("Cleaning the CategorizedArticles collection...");
            categorizedCollection.deleteMany(new Document());

            // Fetch uncategorized articles
            List<Document> uncategorizedArticles = new ArrayList<>();
            articlesCollection.find(new Document("category", null)).into(uncategorizedArticles);

            // Determine the optimal number of threads
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            int threadCount = Math.min(availableProcessors, uncategorizedArticles.size());

            System.out.println("Using " + threadCount + " threads for categorization...");

            // Create a thread pool
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

            // Divide work among threads
            List<Future<Void>> futures = new ArrayList<>();
            int batchSize = Math.max(1, uncategorizedArticles.size() / threadCount);

            for (int i = 0; i < threadCount; i++) {
                int start = i * batchSize;
                int end = (i == threadCount - 1) ? uncategorizedArticles.size() : start + batchSize;

                List<Document> batch = uncategorizedArticles.subList(start, end);

                futures.add(executorService.submit(() -> {
                    processBatch(batch, categorizedCollection);
                    return null;
                }));
            }

            // Wait for all threads to complete
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    System.err.println("Error during batch processing: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Shutdown the thread pool
            executorService.shutdown();
            System.out.println("Categorization completed successfully.");
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processBatch(List<Document> batch, MongoCollection<Document> categorizedCollection) {
        for (Document article : batch) {
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

            if ((content != null && !content.isEmpty()) || (description != null && !description.isEmpty()) || (title != null && !title.isEmpty())) {
                String category = categorizeArticleByKeywords(content, description, title);

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

    private static String categorizeArticleByKeywords(String content, String description, String title) {
        // Combine content, description, and title into a single text block
        String combinedText = (content == null ? "" : content) + " " +
                (description == null ? "" : description) + " " +
                (title == null ? "" : title);

        // Preprocess the combined text
        String processedText = preprocessText(combinedText);
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

    public static String preprocessText(String text) {
        return text.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").trim();
    }
}