/**
 * Created by IntelliJ IDEA.
 * User: Сергей
 * Date: 23.09.2010
 * Time: 22:47:48
 */
public class LibEntry {
    private String authorName;
    private String bookName;
    private String serieName;
    private String bookCode;
    private boolean readed;
    private int id;

    public LibEntry(String authorName, String bookName, String serieName, String bookCode, boolean readed) {
        this.authorName = authorName;
        this.bookName = bookName;
        this.serieName = serieName;
        this.bookCode = bookCode;
        this.readed = readed;
    }

    public LibEntry(String bookCode, boolean readed) {
        this.bookCode = bookCode;
        this.readed = readed;
    }

    public LibEntry() {
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getSerieName() {
        return serieName;
    }

    public void setSerieName(String serieName) {
        this.serieName = serieName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookCode() {
        return bookCode;
    }

    public void setBookCode(String bookCode) {
        this.bookCode = bookCode;
    }

    public boolean isReaded() {
        return readed;
    }

    public void setReaded(boolean readed) {
        this.readed = readed;
    }
}
