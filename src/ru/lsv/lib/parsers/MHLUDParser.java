package ru.lsv.lib.parsers;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.lsv.lib.library.LibraryStorage;

import java.io.*;

/**
 * Парсер .mhlud файла - экспорта из MyHomeLib
 */
public class MHLUDParser {

    /**
     * Загружает список прочитанных книг в текущую активную библиотеку
     * Список прочитанных берется в формате MyHomeLib
     *
     * @param fileName Имя файла, откуда грузить
     * @return 0 - загрузка прошла успешно
     *         -1 - ошибки при загрузке
     *         -2 - нет активной библиотеки
     * @throws java.io.IOException При ошибках чтения
     */
    public static int doLoadReaded(String fileName) throws IOException {

        if (LibraryStorage.getSelectedLibrary() == null) return -2;

        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return -1;
        }

        Session sess = LibraryStorage.getSelectedLibrary().openSession();
        Transaction trx = null;
        try {
            trx = sess.beginTransaction();
            Query query = sess.createQuery("update Book set readed=true where id=?");
            boolean reading = false;
            while (in.ready()) {
                String id = in.readLine();
                if (id == null) break;
                if (!reading) {
                    if (id.startsWith("# Прочитанное")) {
                        reading = true;
                    }
                } else {
                    if (id.startsWith("#")) {
                        reading = false;
                    } else {
                        // Формат mhlud - id[пробел]100. Сплиттим
                        String[] split = id.split(" ");
                        if (split.length == 2) {
                            id = split[0];
                            query.setString(0, id).executeUpdate();
                        }
                    }
                }
            }
            trx.commit();
            trx = null;
        } finally {
            if (trx != null) trx.rollback();
            sess.close();
        }
        return 0;
    }

}
