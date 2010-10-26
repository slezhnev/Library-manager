package ru.lsv.lib.parsers;

/**
 * Интерфейс парсера, который должен обрабатывать папку с библиотекой,
 * определять не появилось ли чего нового - и, если что, то распарсивать
 * новое
 * User: lsv
 * Date: 26.10.2010
 * Time: 11:37:50
 */
public interface LibraryDiffParser {

    /**
     * Проверка на наличие новых архивов в папке с библиотекой
     * @param libPath Путь, где лежит библиотека
     * @return 0 - если все старое, 1 - если есть что-то новое, <0 - если что-то рухнуло
     */
    public int IsNewBooksPresent ( String libPath );

    /**
     * Добавление новых архивов в программу
     * @param libPath Путь, где лежит библиотека
     * @param listener Листенер при обработке файлов. См @FileParserListener
     * @return 0 - если все прочитано удачно, <0 - если что-то унутре рухнуло
     */
    public int processNewBooks ( String libPath, FileParserListener listener);

}
