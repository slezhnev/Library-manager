package ru.lsv.lib.common;

import java.util.HashSet;
import java.util.Set;

/**
 * Хранилище для книги 
 * User: lsv
 * Date: 20.10.2010
 * Time: 12:36:07
 * To change this template use File | Settings | File Templates.
 */
public class Book {

    private Integer bookId;
    private Integer id;
    private Set<Author> authors;
    private String title;
    private String genre;
    private String language;
    private String sourceLanguage;
    private String serieName;
    private Integer numInSerie;
    private String zipFileName;

    public Book() {
        authors = new HashSet<Author>();
    }

    public Book(String zipFileName, int _id, String title, String genre, String language, String sourceLanguage, String serieName, Integer numInSerie, HashSet<Author> authors, int id) {
        this.zipFileName = zipFileName;
        this.id = _id;
        this.title = title;
        this.genre = genre;
        this.language = language;
        this.sourceLanguage = sourceLanguage;
        this.serieName = serieName;
        this.numInSerie = numInSerie;
        this.authors = authors;
        this.id = id;
    }

    public Set<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getSerieName() {
        return serieName;
    }

    public void setSerieName(String serieName) {
        this.serieName = serieName;
    }

    public Integer getNumInSerie() {
        return numInSerie;
    }

    public void setNumInSerie(Integer numInSerie) {
        this.numInSerie = numInSerie;
    }

    public String toString() {
        String S = "authors:\n";
        for (Author author : authors) {
            S = S + author + "\n";
        }
        return S + "title: "+title+"\ngenre: "+genre+"\nlang: "+language+"\nsrc-lang: "+sourceLanguage+"\nserieName: "+serieName+
                "\nnumInSerie: "+numInSerie+"\nzipFile: "+zipFileName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getZipFileName() {
        return zipFileName;
    }

    public void setZipFileName(String zipFileName) {
        this.zipFileName = zipFileName;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }
}

