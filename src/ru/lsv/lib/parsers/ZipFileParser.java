package ru.lsv.lib.parsers;

import org.xml.sax.SAXException;
import ru.lsv.lib.common.Book;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Rласс обработчика zip-архива с книгами
 * User: lsv
 * Date: 20.10.2010
 * Time: 16:15:48
 */
public class ZipFileParser {

    // На всякий случай - создадим синхронизированный лист листенеров. Мало ли
    private List<FileParserListener> listeners = Collections.synchronizedList(new ArrayList<FileParserListener>());

    /**
     * Добавление листенера
     *
     * @param listener листенер
     */
    public void addListener(FileParserListener listener) {
        listeners.add(listener);
    }

    /**
     * Удаление листенера
     *
     * @param listener листенер
     */
    public void removeListener(FileParserListener listener) {
        listeners.remove(listener);
    }

    /**
     * Парсинг zip-файла с книгами
     * На текущий момент в нем должны лежать файлы вида XXXXXXXX.fb2,
     * где XXXXXXX - цифровой код книги
     * @param pathToFile Имя файла архива для обработки
     * @return Сформированный список книг в этом архиве
     * @throws IOException В случае проблем с чтением архива
     * @throws java.text.ParseException В случае, если возникают проболемы с парсингом имени файла в архиве или самой книги
     */
    public List<Book> parseZipFile(String pathToFile) throws IOException, ParseException {
        ZipFile zip = new ZipFile(pathToFile);
        int totalFiles = 0;
        for (Enumeration e = zip.entries(); e.hasMoreElements();) {
            totalFiles++;
            e.nextElement();
        }
        // Вызываем всех листенеров
        for (FileParserListener listener : listeners) {
            listener.filesCounted(totalFiles);            
        }
        // Поехали обрабатывать...
        ArrayList<Book> res = new ArrayList<Book>();
        BookParser bp = new BookParser();
        String fileName = new File(pathToFile).getName(); 
        for (Enumeration e = zip.entries(); e.hasMoreElements();) {
            ZipEntry ze = (ZipEntry)e.nextElement();
            if (!ze.isDirectory()) {
                // Пробуем парсить наименование книги
                String name = ze.getName();
                int id;
                try {
                    id = Integer.parseInt(name.substring(0, name.indexOf(".")));
                } catch (NumberFormatException ex) {
                    throw new ParseException("Invalid filename - "+name, 0);
                }
                try {
                    Book book = bp.parseFB2Stream(zip.getInputStream(ze), id, fileName);
                    res.add(book);
                    // Поехали отфигарим по листенерам
                    for (FileParserListener listener : listeners) {
                        listener.fileProcessed(name, book);            
                    }
                } catch (SAXException e1) {
                    throw new ParseException(e1.getMessage(), 0);
                } catch (ParserConfigurationException e1) {
                    throw new ParseException(e1.getMessage(), 0);
                }
            }
        }
        return res;
    }

}
