package ru.lsv.lib.library;

import org.hibernate.*;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import ru.lsv.lib.library.librusec.LibRusEcLibrary;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Конкретная библиотека
 * User: Lsv
 * Date: 06.11.2010
 * Time: 17:18:52
 */
public class Library {

    /**
     * Используемая factory
     */
    private SessionFactory libraryFactory = null;

    /**
     * Реализация конкретной библиотеки
     */
    private LibraryRealization libraryRealization = null;

    /**
     * Наименование ключевого поля при хранении
     */
    public static final String PRIMARY_KEY = "LIBRARY_ID";

    /**
     * Ключ при хранении
     */
    private Integer libraryId;
    /**
     * Название библиотеки
     */
    private String name;
    /**
     * Место, где лежат файлы библиотеки
     */
    private String storagePath;
    /**
     * Место, где лежит база данных библитеки
     */
    private String dbPath;
    /**
     * Тип библиотеки. Требуется для ассоциации с чем-нибудь внешним
     * поддерживаемые типы:
     * 0 - просто библиотека, отсутствуют все внешние связи
     * 1 - копия библиотеки lib.rus.ec. Поддерживается просмотр данных из нее
     * 2 - копия библиотеки flibusta.net. Поддерживается просмотр данных из нее
     */
    private Integer libraryKind;

    public Library(String name, String storagePath, String dbPath, Integer libraryKind) {
        this.name = name;
        this.storagePath = storagePath;
        this.dbPath = dbPath;
        this.libraryKind = libraryKind;
    }

    public Library() {
    }

    public Integer getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(Integer libraryId) {
        this.libraryId = libraryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public Integer getLibraryKind() {
        return libraryKind;
    }

    public void setLibraryKind(Integer libraryKind) {
        this.libraryKind = libraryKind;
    }


    /**
     * Подготавливает библиотеку к работе.
     * Создает connection factory и т.п.
     *
     * @return 0 - если все нормально
     *         -1 - не используется
     *         -2 - не могу найти путь к storage'у
     *         -3 - проблемы при создании db
     *         -4 - работа с таким типом библиотеки не поддерживается
     */
    public int prepare() {
        Configuration conf = new Configuration().configure("ru/lsv/lib/resources/library.cfg.xml");
        // Изменяем путь до базы
        conf.setProperty("hibernate.connection.url", "jdbc:hsqldb:file:%libdb%;shutdown=true;hsqldb.default_table_type=cached".replace("%libdb%", dbPath));
        try {
            libraryFactory = conf.buildSessionFactory();
        } catch (HibernateException ex) {
            libraryFactory = null;
            libraryRealization = null;
            return -3;
        }
        File storage = new File(storagePath);
        if (!storage.isDirectory()) {
            libraryFactory = null;
            libraryRealization = null;
            return -2;
        }
        switch (libraryKind) {
            case 0:
                return -4;
            case 1: {
                libraryRealization = new LibRusEcLibrary();
                return 0;
            }
            case 2:
                return -4;
            default:
                return -4;
        }
    }

    /**
     * Создание сессии для работы с БД библиотеки
     *
     * @return Созданная сессия или null, если не был вызван prepare или в процессе его работы были проблемы
     */
    public Session openSession() {
        if (libraryFactory == null) return null;
        Session sess = libraryFactory.openSession();
        sess.setFlushMode(FlushMode.COMMIT);
        sess.setCacheMode(CacheMode.NORMAL);
        return sess;
    }

    /**
     * Получение реализации библиотеки
     * @return Реализация
     */
    public LibraryRealization getLibraryRealization() {
        return libraryRealization;
    }

    /**
     * Завершает работу с библиотекой.
     * Без завершения - данные могут быть не сохранены
     */
    public void shutdownLibrary() {
        openSession().createSQLQuery("SHUTDOWN").executeUpdate();
    }

    @Override
    public String toString() {
        return "library: id="+libraryId+", name="+name+", storagePath=" +storagePath+
                ", dbPath="+dbPath+", kind="+libraryKind+", initialized="+((libraryFactory == null) ? "no" : "yes")+
                ", realizationClassName="+(libraryRealization == null ? "no" : libraryRealization.getClass().getName());
    }
}
