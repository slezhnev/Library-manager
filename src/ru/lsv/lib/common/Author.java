package ru.lsv.lib.common;

/**
 * Автор книги
 * User: lsv
 * Date: 20.10.2010
 * Time: 14:52:06
 */
public class Author {

    private Integer authorId;
    private String firstName;
    private String middleName;
    private String lastName;

    public Author() {        
    }

    public Author(String firstName, String middleName, String lastName) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
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
        return "firstName: "+firstName+"; middleName: "+middleName+"; lastName: "+lastName;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }
}
