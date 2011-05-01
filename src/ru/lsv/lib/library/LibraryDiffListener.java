package ru.lsv.lib.library;

/**
 * Интерфейс реализации callback'ов при обработке diff'а библиотеки
 * User: Lsv
 * Date: 07.11.2010
 * Time: 19:04:18
 */
public interface LibraryDiffListener {

    /**
     * Посчитано общее количество файлов в дифе
     * @param totalFilesInDiff Общее количество файлов в дифе
     */
    void totalFilesInDiffCounted ( int totalFilesInDiff );

    /**
     * Начата обработка нового файла
     * @param fileName Имя файла
     */
    void beginNewFile (String fileName);

    /**
     * При обработке файла прилетел exception
     * @param fileName Имя файла
     * @param msg Сообщение exception'а
     */
    void fileProcessFailed (String fileName, String msg);

    /**
     * Начали процесс сохранения книг (он длительный)
     * @param fileName Имя файла
     */
    void fileProcessSavingBooks (String fileName);

}
