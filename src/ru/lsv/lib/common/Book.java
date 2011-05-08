package ru.lsv.lib.common;

import java.util.*;

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
    private List<Author> authors;
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

    /**
     * Аннотация к книге
     */
    private String annotation;

    /**
     * Отметка о том, что книга прочитана
     */
    private Boolean readed;

    /**
     * Отметка о том, что книгу неплохо было бы прочитать
     */
    private Boolean mustRead;


    public Book() {
        authors = new ArrayList<Author>();
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
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

    public String formHTMLDescription() {
        StringBuffer str = new StringBuffer("<html><b>Название:</b>&nbsp;&nbsp;");
        if (title == null) str.append("Нет названия");
        else str.append(title);
        str.append("<br>");
        if (serieName != null) {
            str.append("<b>Серия:</b>&nbsp;&nbsp;").append(serieName);
            str.append(" - ");
            if (numInSerie == null) str.append("б/н");
            else str.append(numInSerie);
            str.append("<br><br>");
        }
        str.append("<b>Авторы</b>:<br>");
        for (Author author : authors) {
            str.append(author.makeName()).append("<br>");
        }
        str.append("<br>");
        str.append("<b>Архив:</b><br>").append(zipFileName).append("<br>");
        str.append("<b>Имя файла:</b><br>").append(id);
        return str.toString();
    }

    public String toLongString() {
        String S = "authors:\n";
        for (Author author : authors) {
            S = S + author + "\n";
        }
        return S + "bookId: " + bookId + " id: " + id + " title: " + title + "\ngenre: " + genre + "\nlang: " + language + "\nsrc-lang: " + sourceLanguage + "\nserieName: " + serieName +
                "\nnumInSerie: " + numInSerie + "\nzipFile: " + zipFileName + "\nCRC: " + crc32;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        if (title == null) str.append("Нет названия");
        else str.append(title);
        if (serieName != null) {
            str.append(" (").append(serieName).append(" - ");
            if (numInSerie != null)
                str.append(numInSerie);
            else
                str.append("нет");
            str.append(")");
        }
        return str.toString();
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

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public Boolean getReaded() {
        return readed;
    }

    public void setReaded(Boolean readed) {
        this.readed = readed;
    }

    public Boolean getMustRead() {
        return mustRead;
    }

    public void setMustRead(Boolean mustRead) {
        this.mustRead = mustRead;
    }

    /**
     * Создает текстовое представление автора (или "Сборник", если авторов несколько)
     * Используется при экспорте книг
     *
     * @return Текстовое представление автора/ов
     */
    public String getAuthorsToString() {
        if (authors.size() >= 1) {
            Author author = authors.get(0);
            return author.makeName();
        } else {
            return "Без автора";
        }
    }


}

