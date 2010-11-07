package ru.lsv.lib.parsers;

import ru.lsv.lib.common.Book;

/**
 * Класс организации callback'ов при парсинге zip-файла библиотеки.
 * Ибо иначе оно там умедитируется
 * User: дым
 * Date: 20.10.2010
 * Time: 16:41:09
 */
public interface FileParserListener {

    void inArchiveFilesCounted( int numFilesInZip );

    void inArchiveFileProcessed( String fileName, Book book );

}
