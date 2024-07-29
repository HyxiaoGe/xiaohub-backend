package com.xiaohub.datadigger.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Article {

    private String content;
    private String link;
    private String publicationDate;
    private String title;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @JsonProperty("publication_date")
    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Article article = (Article) obj;

        return Objects.equals(link, article.link) && Objects.equals(title, article.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(link, title);
    }
}
