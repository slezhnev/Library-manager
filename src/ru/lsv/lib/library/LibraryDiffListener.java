package ru.lsv.lib.library;

/**
 * Интерфейс реализации callback'ов при обработке diff'а библиотеки
 * User: Lsv
 * Date: 07.11.2010
 * Time: 19:04:18
 */
public interface LibraryDiffListener {

    void totalFilesInDiffCounted ( int totalFilesInDiff );

    void beginNewFile (String fileName);

}
