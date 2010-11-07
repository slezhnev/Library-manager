package ru.lsv.lib.library;

import ru.lsv.lib.parsers.FileParserListener;

/**
 * Интерфейс непосредственной работы с библиотекой
 * User: Lsv
 * Date: 07.11.2010
 * Time: 13:48:42
 */
public interface LibraryRealization {
    /**
     * Проверка на наличие новых архивов в папке с библиотекой
     *
     * @return 0 - если все старое, 1 - если есть что-то новое, <0 - если что-то рухнуло
     */
    public int IsNewBooksPresent();

    /**
     * Добавление новых архивов в программу
     *
     * @param diffListener Листенер при обработке diff'ов библиотеки
     * @param fileListener Листенер при обработке файлов внутри архивов
     * @return 0 - если все прочитано удачно
     *         -1 - рухнуло что-то кардинально и нифига не было добавлено
     *         -2 - рухнуло что-то не кардинально и ВОЗМОЖНО что-то было добавлено
     */
    public int processNewBooks(LibraryDiffListener diffListener, FileParserListener fileListener);
}
