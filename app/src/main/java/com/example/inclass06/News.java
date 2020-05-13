package com.example.inclass06;

public class News {
    String title;
    String publishedAt;
    String imageURL;
    String description;

    public News(String title, String publishedAt, String imageURL, String description) {
        this.title = title;
        this.publishedAt = publishedAt;
        this.imageURL = imageURL;
        this.description = description;
    }

    public News() {

    }

    @Override
    public String toString() {
        return "News{" +
                "title='" + title + '\'' +
                ", publishedAt='" + publishedAt + '\'' +
                ", imageURL='" + imageURL + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
