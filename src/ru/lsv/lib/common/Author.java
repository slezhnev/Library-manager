package ru.lsv.lib.common;

import java.util.HashSet;
import java.util.Set;

/**
 * Автор книги
 * User: lsv
 * Date: 20.10.2010
 * Time: 14:52:06
 */
public class Author {

    public static final String PRIMARY_KEY = "AUTHOR_ID";

    /**
     * Ключ при хранении
     */
    private Integer authorId;
    /**
     * Имя автора
     */
    private String firstName;
    /**
     * Отчество, может отсутствовать
     */
    private String middleName;
    /**
     * Фамилия
     */
    private String lastName;
    /**
     * Книги выбранного автора
     */
    private Set<Book> books;

    public Author() {
        books = new HashSet<Book>();
    }

    public Author(String firstName, String middleName, String lastName, Set<Book> books) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.books = books;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String toString() {
        return "authorId :" + authorId + " firstName: " + firstName + "; middleName: " + middleName + "; lastName: " + lastName;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    @Override
    public boolean equals(Object some) {
        if (some == null) return false;
        if (this == some) return true;
        if (!(some instanceof Author)) return false;
        Author author = (Author) some;
        return Utils.areEqual(author.firstName, author.firstName) &&
                Utils.areEqual(author.middleName, author.middleName) &&
                Utils.areEqual(author.lastName, author.lastName);
    }

    @Override
    public int hashCode() {
        return Utils.getHash(firstName) +
                Utils.getHash(middleName) +
                Utils.getHash(lastName);

    }

    public Set<Book> getBooks() {
        return books;
    }

    public void setBooks(Set<Book> books) {
        this.books = books;
    }
}
