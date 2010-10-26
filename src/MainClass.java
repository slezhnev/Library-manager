import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by IntelliJ IDEA.
 * User: Сергей
 * Date: 23.09.2010
 * Time: 20:50:28
 */
public class MainClass {

    private static final SessionFactory ourSessionFactory;

    static {
        try {
            ourSessionFactory = new AnnotationConfiguration().
                    configure("hibernate.cfg.xml").
                    buildSessionFactory();
        }
        catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Session getSession() throws HibernateException {
        return ourSessionFactory.openSession();
    }


    public static void main(String[] argc) {
        // Читаем настройки
        if (System.getProperty("settings") == null) {
            System.out.println("Не могу найти параметры для работы. Необходимо указать -Dsettings=\"файл с настройками\"");
        } else {
            Properties props = new Properties();
            try {
                props.load(new FileReader(System.getProperty("settings")));
            } catch (IOException e) {
                System.out.println("! Ошибка загрузки файла настройки");
                return;
            }
            // Вначале нам надо загрузить список читанных...
            BufferedReader usrReader = null;
            try {
                usrReader = new BufferedReader(new InputStreamReader(new FileInputStream(props.getProperty("mhlud")), "UTF-8"));
            } catch (IOException e) {
                System.out.println("! Ошибка чтения mhlud файла");
            }
            ArrayList<String> readedList = new ArrayList<String>();
            if (usrReader != null) {
                boolean readReaded = false;
                try {
                    while (usrReader.ready()) {
                        String s = usrReader.readLine();
                        if (s.startsWith("#")) readReaded = false;
                        if (readReaded) readedList.add(s.trim().split(" ")[0]);
                        else {
                            if ("# Прочитанное".equals(s)) readReaded = true;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("! Ошибка чтения mhlud файла");
                }
            }
            //
            // Поехали грузить в hsql всю библиотеку
            final Session sess = getSession();
            ZipFile inpx;
            try {
                inpx = new ZipFile(props.getProperty("inpx"));
            } catch (IOException e) {
                System.out.println("! Ошибка открытия inpx файла");
                return;
            }
            Enumeration entries = inpx.entries();
            int totalProcessed = 0;
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if ((!entry.isDirectory()) && (!entry.getName().toLowerCase().equals("version.info"))) {
                    // Тут типа надо будет читать...
                    System.out.println("Читаем " + entry.getName());
                    BufferedReader inReader = null;
                    try {
                        inReader = new BufferedReader(new InputStreamReader(inpx.getInputStream(entry), "UTF-8"));
                    } catch (IOException e) {
                        System.out.println("! Ошибка получения потока чтения из архива для файла " + entry.getName());
                    }
                    if (inReader != null) {
                        try {
                            inReader.read();
                            while (inReader.ready()) {
                                String[] splitted = inReader.readLine().split("" + (char) 0x04);
                                if (!splitted[8].equals("1")) {
                                    LibEntry lEntry = new LibEntry(splitted[0], splitted[2], splitted[3], splitted[7],
                                            isReaded(readedList, splitted[7]));
                                    sess.save(lEntry);
                                    totalProcessed++;
                                    if ((totalProcessed % 1000) == 0)
                                        System.out.println("Загружено - " + totalProcessed + " книг");
                                }
                                //return;
                            }
                        } catch (IOException e) {
                            System.out.println("! Ошибка чтения файла " + entry.getName() + " из архива");
                        }
                    }
                }
            }
            System.out.println("Загрузка библиотеки завершена. Всего загружено " + totalProcessed + " книг");
            //
            // Вот теперь поехали получать distinct набор автор+название серии+readed
            Iterator series = sess.createQuery("select distinct authorName, serieName from LibEntry where readed=true order by authorName, serieName").iterate();
            while (series.hasNext()) {
                Object[] row = (Object[]) series.next();
                String authorName = (String) row[0];
                String serieName = (String) row[1];
                if (serieName.trim().length() != 0) {
                    List seriesBooks = sess.createQuery("select distinct bookName from LibEntry where authorName=? AND serieName=? AND readed=false").
                            setString(0, authorName).setString(1, serieName).list();
                    if (seriesBooks.size() > 0) {
                        System.out.println("Автор: " + authorName + " \\ серия : " + serieName);
                        for (Object row1 : seriesBooks) {
                            // А вот тут спешить не будем. Проверим - а может быть у нас есть уже прочитанная книга с таким названием?
                            List isReaded = sess.createQuery("from LibEntry where authorName=? AND bookName=? AND readed=true ").
                                    setString(0, authorName).setString(1, (String) row1).list();
                            if (isReaded.size() == 0)
                                System.out.println(" Книга: " + row1);
                        }
                    }
                }
            }
            //
        }
    }

    private static boolean isReaded(ArrayList<String> readedList, String bookCode) {
        for (String readedCode : readedList) {
            if (bookCode.equals(readedCode)) return true;
        }
        return false;
    }

}
