package ru.lsv.lib.common;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
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
//    private static final Session currSession;

    static {
        try {
            ourSessionFactory = new AnnotationConfiguration().
                    configure("hibernate.cfg.xml").
                    buildSessionFactory();
            /*currSession = ourSessionFactory.openSession();
            currSession.setFlushMode(FlushMode.COMMIT);
            currSession.setCacheMode(CacheMode.NORMAL);*/
        }
        catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Получение текущей сессии работы с Hibernate
     *
     * @return Текущую сессию
     */
    public static Session getSession() {
        Session currSession;
        currSession = ourSessionFactory.openSession();
        currSession.setFlushMode(FlushMode.COMMIT);
        currSession.setCacheMode(CacheMode.NORMAL);
        //return ourSessionFactory.openSession();
        return currSession;
    }

}
