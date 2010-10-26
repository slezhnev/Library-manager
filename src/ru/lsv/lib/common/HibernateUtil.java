package ru.lsv.lib.common;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

/**
 * Всякая вспомогательность при работе с гибернейтом
 * User: lsv
 * Date: 21.10.2010
 * Time: 15:47:36
 */
public class HibernateUtil {
    private static final SessionFactory ourSessionFactory;
    private static final Session currSession;

    static {
        try {
            ourSessionFactory = new AnnotationConfiguration().
                    configure("hibernate.cfg.xml").
                    buildSessionFactory();
            currSession = ourSessionFactory.openSession();
        }
        catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Session getSession() throws HibernateException {
        //return ourSessionFactory.openSession();
        return currSession;
    }

    public static Author getAuthorFromDB(Author author) {
        Author tmpAuthor = (Author) getSession().createQuery("from Author where firstName=? AND middleName=? AND lastName=? ").
                setString(0, author.getFirstName()).setString(1, author.getMiddleName()).setString(2, author.getLastName()).uniqueResult();
        if (tmpAuthor == null) {
            getSession().save(author);
            return author;
        } else
            return tmpAuthor;
    }

}
