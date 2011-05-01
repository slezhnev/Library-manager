package ru.lsv.lib.library;

import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
import ru.lsv.lib.common.Author;
import ru.lsv.lib.common.Book;

/**
 * Класс работы с библиотекой в Hibernate
 */
public class LibraryUtils {
    /**
     * Ищет указанного автора в базе. В случае отсутствия его там - добавляет
     *
     * @param author Автора, которого надо найти в базе
     * @return Найденный в базе автор или сохраненный автор. null, если что-то упало в процессе поиска/записи в базу
     */
    public static Author getAuthorFromDB(Author author) {
        if (LibraryStorage.getSelectedLibrary() == null) return null;
        Session sess = null;
        Transaction trx = null;
        try {
            sess = LibraryStorage.getSelectedLibrary().openSession();
            if (sess == null) return null;
            Criteria crit = sess.createCriteria(Author.class);
            if (author.getFirstName() != null) crit.add(Restrictions.eq("firstName", author.getFirstName()));
            else crit.add(Restrictions.isNull("firstName"));
            if (author.getMiddleName() != null) crit.add(Restrictions.eq("middleName", author.getMiddleName()));
            else  crit.add(Restrictions.isNull("middleName"));
            if (author.getLastName() != null) crit.add(Restrictions.eq("lastName", author.getLastName()));
            else  crit.add(Restrictions.isNull("lastName"));
            Author tmpAuthor = (Author) crit.uniqueResult();
            if (tmpAuthor == null) {
                trx = sess.beginTransaction();
                sess.save(author);
                sess.flush();
                trx.commit();
                sess.close();
                return author;
            } else
                return tmpAuthor;
        } catch (HibernateException ex) {
            if (trx != null) trx.rollback();
            if (sess != null) sess.close();
            return null;
        }
    }

    /**
     * Добавляет книгу в библиотечный список
     * Предварительно делается проверка по названию, серии (если есть), авторам и crc
     * Если такая книга в библиотеке есть - то она не добавляется
     *
     * @param book Книга для добавления в библиотеку
     * @return true, если книга добавилась нормально. false - иначе
     */
    public static boolean addBookToLibrary(Book book) {
        if (LibraryStorage.getSelectedLibrary() == null) return false;
        Session sess = null;
        Transaction trx = null;
        try {
            sess = LibraryStorage.getSelectedLibrary().openSession();
            if (sess == null) return false;
            //StringBuffer str = new StringBuffer("select count(bookId) from Book where title=? AND genge=? AND crc32=?");
            /*if ((book.getSourceLanguage() != null) && (book.getSourceLanguage().length() > 0)) {
                str.append(" AND sourceLanguage=?");
            }
            if ((book.getLanguage() != null) && (book.getLanguage().length() > 0)) {
                str.append(" AND language=?");
            }
            if ((book.getSerieName() != null) && (book.getSerieName().length() > 0)) {
                str.append(" AND serieName=? AND numInSerie=?");
            }*/
            /*Query query = sess.createQuery("select count(bookId) from Book where title=? AND genre=? AND crc32=?")
                    .setString(0, book.getTitle())
                    .setString(1, book.getGenre())
                    .setLong(2, book.getCrc32());
            Long count = (Long) query.uniqueResult();*/
            /*Criteria crit = sess.createCriteria(Book.class);
            crit.add(Restrictions.eq("title", book.getTitle()));
            crit.add(Restrictions.eq("genre", book.getGenre()));
            crit.add(Restrictions.eq("crc32", book.getCrc32()));
            if ((book.getSourceLanguage() != null) && (book.getSourceLanguage().length() > 0)) {
                crit.add(Restrictions.eq("sourceLanguage", book.getSourceLanguage()));
            }
            if ((book.getLanguage() != null) && (book.getLanguage().length() > 0)) {
                crit.add(Restrictions.eq("language", book.getLanguage()));
            }
            if ((book.getSerieName() != null) && (book.getSerieName().length() > 0)) {
                crit.add(Restrictions.eq("serieName", book.getSerieName()));
                crit.add(Restrictions.eq("numInSerie", book.getNumInSerie()));
            }*/
            /*for (Author author : book.getAuthors()) {
                crit.add(Restrictions.sqlRestriction("? = some(select " + Author.PRIMARY_KEY + " from BOOK_AUTHORS ba " +
                        "where {alias}." + Book.PRIMARY_KEY + " = ba." + Book.PRIMARY_KEY + ")",
                        author.getAuthorId(), Hibernate.INTEGER));
            }*/
            //if (crit.list().size() == 0) {
            if (true/*count == 0*/) {
                trx = sess.beginTransaction();
                sess.save(book);
                sess.flush();
                trx.commit();
            }
            return true;
        } catch (HibernateException ex) {
            if (trx != null) trx.rollback();
            if (sess != null) sess.close();
            return false;
        }

    }
}