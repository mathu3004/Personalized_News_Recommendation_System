package PersonalizedNews;

import javafx.scene.control.RadioButton;

public class Article {
    private RadioButton select;
    private int articleId;
    private String title;
    private String author;
    private String description;
    private String publishedDate;
    private String category;
    private String content;

    public Article(int articleId, String title, String author, String description, String publishedDate, String content, String category) {
        this.articleId = articleId;
        this.title = title;
        this.author = author;
        this.description = description;
        this.publishedDate = publishedDate;
        this.content = content;
        this.category = category;
    }

    public Article(int articleId, String category, String author, String publishedDate, String title, String description, RadioButton select) {
        this.articleId = articleId;
        this.category = category;
        this.author = author;
        this.publishedDate = publishedDate;
        this.title = title;
        this.description = description;
        this.select = select;
    }

    public int getArticleId() { return articleId; }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public String getContent() {
        return content;
    }

    public String getCategory() {
        return category;
    }

    public RadioButton getSelect() {
        return select;
    }
}
