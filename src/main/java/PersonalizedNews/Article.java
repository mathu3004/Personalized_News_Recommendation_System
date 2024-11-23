package PersonalizedNews;

import javafx.scene.control.RadioButton;

public class Article {
    private RadioButton select;
    private int articleId;
    private String title;
    private String author;
    private String description;
    private String publishedAt;
    private String publishedDate;
    private String content;
    private String category;

    public Article(int articleId, String title, String author, String description, String publishedAt, String content, String category) {
        this.articleId = articleId;
        this.title = title;
        this.author = author;
        this.description = description;
        this.publishedAt = publishedAt;
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

    public int getArticleId() {
        return articleId;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getPublishedAt() {
        return publishedAt;
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
