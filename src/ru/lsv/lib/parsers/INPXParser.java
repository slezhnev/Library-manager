package ru.lsv.lib.parsers;

import org.apache.commons.io.FilenameUtils;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Класс-парсер INPX файла библиотеки
 */
public class INPXParser {

    private Map<String, INPRecord> records = new TreeMap<String, INPRecord>();

    /**
     * Конструктор
     *
     * @param inpxFileName Имя inpx-файла для обработки
     * @throws java.io.IOException В случае проблем чтения из inpx-файла
     */
    public INPXParser(String inpxFileName) throws IOException {
        ZipFile zip = new ZipFile(inpxFileName);
        for (Enumeration e = zip.entries(); e.hasMoreElements();) {
            ZipEntry ze = (ZipEntry) e.nextElement();
            if ("inp".equals(FilenameUtils.getExtension(ze.getName()))) {
                // Значит это INP-файл
                // В связи с тем, что, к примеру, в Либрусеке оно может быть в 1251 и в UTF-8 - придется
                // детектировать чарсет. К сожалению. Как определить "на лету" - я что-то хз
                //
                // Пример использования из http://code.google.com/p/juniversalchardet/
                byte[] buf = new byte[4096];
                InputStream inps = zip.getInputStream(ze);
                UniversalDetector detector = new UniversalDetector(null);

                // (2)
                int nread;
                while ((nread = inps.read(buf)) > 0 && !detector.isDone()) {
                    detector.handleData(buf, 0, nread);
                }
                // (3)
                detector.dataEnd();

                // (4)
                String encoding = detector.getDetectedCharset();
                //
                // Получили кодировку
                BufferedReader binp = new BufferedReader(new InputStreamReader(zip.getInputStream(ze), encoding));
                String inpLine;
                // Читаем
                while ((inpLine = binp.readLine()) != null) {
                    try {
                        INPRecord inpr = new INPRecord(inpLine);
                        records.put(inpr.getFile(), inpr);
                    } catch (INPRecord.BadINPRecord ignored) {
                        // В случае возникновения exception - просто ничего никуда не добавится                        
                    }
                }
            }
        }
    }

    /**
     * Получение сформированного списка элементов INPX файла
     *
     * @return см.описание
     */
    public Map<String, INPRecord> getRecords() {
        return records;
    }


}
