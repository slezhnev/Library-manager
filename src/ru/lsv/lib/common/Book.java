package ru.lsv.lib.common;

import java.util.Date;
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

    public static final String PRIMARY_KEY = "BOOK_ID";

    /**
     * Ключ при хранении
     */
    private Integer bookId;
    /**
     * Имя файла этой книги в zipFileName
     */
    private String id;
    /**
     * Список авторов
     */
    private Set<Author> authors;
    /**
     * Название
     */
    private String title;
    /**
     * Жанр
     */
    private String genre;
    /**
     * Язык
     */
    private String language;
    /**
     * Исходный язык (для переводных книг)
     */
    private String sourceLanguage;
    /**
     * Название серии (если есть)
     */
    private String serieName;
    /**
     * Номер в серии (если есть)
     */
    private Integer numInSerie;
    /**
     * Zip файл, где лежит книга
     */
    private String zipFileName;
    /**
     * CRC32 книги. Нужно для устранения дублей в библиотеке
     */
    private Long crc32;
    /**
     * Время добавления книги в библиотеку
     */
    private Date addTime; 

    public Book() {
        authors = new HashSet<Author>();
    }

    public Book(String zipFileName, String _id, String title, String genre, String language, String sourceLanguage, String serieName, Integer numInSerie, HashSet<Author> authors, int id,
                long crc32, Date addTime) {
        this.zipFileName = zipFileName;
        this.id = _id;
        this.title = title;
        this.genre = genre;
        this.language = language;
        this.sourceLanguage = sourceLanguage;
        this.serieName = serieName;
        this.numInSerie = numInSerie;
        this.authors = authors;
        this.crc32 = crc32;
        this.addTime = addTime;
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
        return S + "bookId: " + bookId + " id: " + id + " title: " + title + "\ngenre: " + genre + "\nlang: " + language + "\nsrc-lang: " + sourceLanguage + "\nserieName: " + serieName +
                "\nnumInSerie: " + numInSerie + "\nzipFile: " + zipFileName + "\nCRC: " + crc32;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public Long getCrc32() {
        return crc32;
    }

    public void setCrc32(Long crc32) {
        this.crc32 = crc32;
    }

    @Override
    public int hashCode() {
        return Utils.getHash(bookId) +
                Utils.getHash(id) +
                Utils.getHash(authors) +
                Utils.getHash(title) +
                Utils.getHash(genre) +
                Utils.getHash(language) +
                Utils.getHash(sourceLanguage) +
                Utils.getHash(serieName) +
                Utils.getHash(numInSerie) +
                Utils.getHash(zipFileName) +
                Utils.getHash(crc32);
    }

    @Override
    public boolean equals(Object some) {
        if (some == null) return false;
        if (this == some) return true;
        if (!(some instanceof Book)) return false;
        Book book = (Book) some;
        return Utils.areEqual(this.authors, book.authors) &&
                Utils.areEqual(this.bookId, book.bookId) &&
                Utils.areEqual(this.genre, book.genre) &&
                Utils.areEqual(this.title, book.title) &&
                Utils.areEqual(this.id, book.id) &&
                Utils.areEqual(this.language, book.language) &&
                Utils.areEqual(this.serieName, book.serieName) &&
                Utils.areEqual(this.numInSerie, book.numInSerie) &&
                Utils.areEqual(this.crc32, book.crc32) &&
                Utils.areEqual(this.sourceLanguage, book.sourceLanguage) &&
                Utils.areEqual(this.zipFileName, book.zipFileName);
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }
}

