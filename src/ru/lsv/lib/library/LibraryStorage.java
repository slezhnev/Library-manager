package ru.lsv.lib.library;

import org.hibernate.*;
import org.hibernate.cfg.AnnotationConfiguration;

import java.util.List;

/**
 * Основной класс работы с библиотекой
 * User: Lsv
 * Date: 06.11.2010
 * Time: 17:09:00
 */
public class LibraryStorage {

    private static SessionFactory librarySessionFactory;
    private static List<Library> libraries;
    private static int selectedLibraryId = -1;
    private static Library selectedLibrary = null;

    static {
        try {
            // Создаем фактори. Тут конфиг ВСЕГДА берется из hibernate.cfg.xml
            librarySessionFactory = new AnnotationConfiguration().configure("hibernate.cfg.xml").buildSessionFactory();
            rebuildLibrariesList(null);
        }
        catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Получение сессии работы с Hibernate
     *
     * @return Сессия или null, если работа с сессией была завершена
     */
    private static Session getSession() {
        if (librarySessionFactory != null) {
            Session sess = librarySessionFactory.openSession();
            sess.setFlushMode(FlushMode.COMMIT);
            sess.setCacheMode(CacheMode.NORMAL);
            return sess;
        } else return null;
    }

    /**
     * Перестроение списка доступных библиотек
     * После перестроения <b>МОЖЕТ БЫТЬ</b> сброшена текущая выбранная библиотека, если она вдруг пропадет из списка
     *
     * @param _sess Сессия для перестройки. Может быть null, тогда сессия будет создана и закрыта унутре
     */
    public static void rebuildLibrariesList(Session _sess) {
        Session sess;
        if (_sess == null) sess = getSession();
        else sess = _sess;
        libraries = sess.createQuery("from Library order by name").list();
        // Поехали проверим - а текущая выбранная библиотека-то еще актуальна?
        selectLibrary(selectedLibraryId);
        //
        if (_sess == null) sess.close();
    }

    /**
     * Получение списка подключенных библиотек
     *
     * @return Список подключенных библиотек. Может иметь нулевую длину, если ничего начисто не подключено
     */
    public static List<Library> getLibraries() {
        return libraries;
    }

    /**
     * Добавление библиотеки. Если добавление прошло успешно - она становится активной.
     *
     * @param library Библиотека для добавления
     * @return Добавленная библиотека или null, если:
     *         1. Библиотека с таким именем уже существует
     *         2. Пытались добавить библиотеку с незаполненным полями
     *         3. Не проходит .prepare у добавленной библиотеки
     * @throws org.hibernate.HibernateException
     *          В случае проблем работы с Hibernate. Для обработки снаружи в интерфейсе
     */
    public static Library addLibrary(Library library) throws HibernateException {
        if ((library.getName() == null) || (library.getName().length() == 0)) return null;
        if ((library.getStoragePath() == null) || (library.getStoragePath().length() == 0)) return null;
        if ((library.getDbPath() == null) || (library.getDbPath().length() == 0)) return null;
        Transaction trx = null;
        Session sess = null;
        try {
            sess = getSession();
            List libs = sess.createQuery("from Library where name=?").setString(0, library.getName()).list();
            if (libs.size() != 0) {
                sess.close();
                return null;
            } else if (library.prepare() != 0) {
                sess.close();
                return null;
            } else {
                // А тут - сохраняем
                trx = sess.beginTransaction();
                sess.save(library);
                sess.flush();
                trx.commit();
                trx = null;
                rebuildLibrariesList(sess);
                sess.close();
                sess = null;
                // Выберем добавленную библиотеку
                if (selectLibrary(library.getLibraryId()) != 0) {
                    throw new HibernateException("Добавленная библиотека не может быть выбрана в качестве текущей");
                }
                //
                return library;
            }
        } catch (HibernateException ex) {
            if (trx != null) trx.rollback();
            if (sess != null) sess.close();
            throw ex;
        }
    }

    /**
     * Выбирает библиотеку с указанным ключем для работы
     *
     * @param libraryId Ключ выбираемой библиотеки
     * @return 0 - библиотека нормально выбралась,
     *         -1 - в списке нет библиотеки с таким ключем
     *         меньше -1 - произошла ошибка при подготовке выбранной библиотеки к работе. См @ru.lsv.lib.library.Library.prepare()
     */
    public static int selectLibrary(int libraryId) {
        for (Library library : libraries) {
            if (libraryId == library.getLibraryId()) {
                if (selectedLibrary != library) {
                    int res = library.prepare();
                    if (res != 0) {
                        return res;
                    }
                    if (selectedLibrary != null)
                        selectedLibrary.shutdownLibrary();
                    selectedLibrary = library;
                    selectedLibraryId = libraryId;
                }
                return 0;
            }
        }
        return -1;
    }

    /**
     * Получение текущей выбранной библиотеки
     *
     * @return Выбранная Библиотека или null, если ничего не выбрано
     */
    public static Library getSelectedLibrary() {
        if ((selectedLibrary == null) || (selectedLibraryId == -1)) return null;
        /*if (!selectedLibrary.getLibraryId().equals(selectedLibraryId)) {
            // Какая-то странная фигня
            selectedLibrary = null;
            selectedLibraryId = -1;
        }*/
        return selectedLibrary;
    }

    /**
     * Завершает работу с выбранной библиотекой
     */
    public static void shutdownSelectedLibrary() {
        if (selectedLibrary != null) {
            selectedLibrary.shutdownLibrary();
            selectedLibrary = null;
            selectedLibraryId = -1;
        }
    }

    /**
     * Завершает работу с объектом
     */
    public static void shutdownStorage() {
        getSession().createSQLQuery("SHUTDOWN").executeUpdate();
    }

}
