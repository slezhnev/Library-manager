import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
import ru.lsv.lib.common.Author;
import ru.lsv.lib.common.Book;
import ru.lsv.lib.common.HibernateUtil;
import ru.lsv.lib.library.LibraryUtils;
import ru.lsv.lib.parsers.FB2ZipFileParser;
import ru.lsv.lib.parsers.FileParserListener;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: admin
 * Date: 13.10.2010
 * Time: 16:20:10
 * To change this template use File | Settings | File Templates.
 */
public class Test {

    public static void main(String[] args) {

//        Enumeration<Object> keys = FB2Genres.genres.keys();
//
//        while (keys.hasMoreElements()) {
//            System.out.println(""+keys.nextElement());
//        }

        //Transaction trx = sess.beginTransaction();
        FB2ZipFileParser zfp = new FB2ZipFileParser();
        zfp.addListener(new FileParserListener() {
            @Override
            public void inArchiveFilesCounted(int numFilesInZip) {
                System.out.println("Total files in zip: " + numFilesInZip);
            }

            @Override
            public void inArchiveFileProcessed(String fileName, Book book) {
                //System.out.println("--Loaded book from: " + fileName + "--\n" + book);
            }
        });

        List<Book> res = null;
        try {
            res = zfp.parseZipFile("H:\\JavaProjects\\Librarian\\test.lib\\fb2-216642-221999.zip");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if (res != null) {
            System.out.println("" + res.size());
            for (Book book : res) {
                //sess.save(book);
                LibraryUtils.addBookToLibrary(book);
            }
        }
        //sess.flush();
        //trx.commit();

        /*System.out.println("\n\nSaved books\n\n");
        for (Object o : sess.createQuery("from Book").list()) {
            System.out.println("" + o + "\n");
        }*/

        Session sess = HibernateUtil.getSession();
        Book firstBook = (Book) sess.createQuery("from Book").list().get(0);
        System.out.println("" + firstBook);

        Criteria crit = sess.createCriteria(Book.class);
        crit.add(Restrictions.eq("title", firstBook.getTitle()));
        crit.add(Restrictions.eq("genre", firstBook.getGenre()));
        crit.add(Restrictions.eq("crc32", firstBook.getCrc32()));
        if ((firstBook.getSourceLanguage() != null) && (firstBook.getSourceLanguage().length() > 0)) {
            crit.add(Restrictions.eq("sourceLanguage", firstBook.getSourceLanguage()));
        }
        if ((firstBook.getLanguage() != null) && (firstBook.getLanguage().length() > 0)) {
            crit.add(Restrictions.eq("language", firstBook.getLanguage()));
        }
        if ((firstBook.getSerieName() != null) && (firstBook.getSerieName().length() > 0)) {
            crit.add(Restrictions.eq("serieName", firstBook.getSerieName()));
            crit.add(Restrictions.eq("numInSerie", firstBook.getNumInSerie()));
        }
        for (Author author : firstBook.getAuthors()) {
            System.out.println("\n authorId: "+author.getAuthorId());
            crit.add(Restrictions.sqlRestriction("? = some(select " + Author.PRIMARY_KEY + " from BOOK_AUTHORS ba "+
                                                 "where {alias}." + Book.PRIMARY_KEY + " = ba." + Book.PRIMARY_KEY + ")",
                    author.getAuthorId(), Hibernate.INTEGER));
        }
        List books = crit.list();
        if (books.size() > 0) {
            System.out.println("\n\nFound:\n" + books.get(0));
        } else {
            System.out.println("\n\nNothing found");
        }        
        sess.createSQLQuery("SHUTDOWN");
        sess.close();
    }

}
