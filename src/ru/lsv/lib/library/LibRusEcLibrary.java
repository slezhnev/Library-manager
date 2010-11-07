package ru.lsv.lib.library;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.lsv.lib.common.Book;
import ru.lsv.lib.common.FileEntity;
import ru.lsv.lib.parsers.FB2ZipFileParser;
import ru.lsv.lib.parsers.FileParserListener;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Реализация работы с библиотекой Либрусек
 * User: Lsv
 * Date: 07.11.2010
 * Time: 13:49:55
 */
public class LibRusEcLibrary implements LibraryRealization {
    /**
     * см. {@link ru.lsv.lib.library.LibraryRealization}
     *
     * @return см. {@link ru.lsv.lib.library.LibraryRealization}
     */
    @Override
    public int IsNewBooksPresent() {
        List<String> newFiles = getFilesDiff();
        if (newFiles == null) return -1;
        return (newFiles.size() == 0 ? 0 : 1);
    }

    /**
     * Получение списка новых zip-файлов - т.е. которые есть в storagePath, но их нет в db
     *
     * @return Сформированный список файлов или null - если что-то не того
     */
    private List<String> getFilesDiff() {
        ArrayList<String> newFiles = new ArrayList<String>();
        if (LibraryStorage.getSelectedLibrary() == null) return null;
        Library library = LibraryStorage.getSelectedLibrary();
        // Получим список zip-файлов в директории
        File storage = new File(library.getStoragePath());
        String[] fileArray = storage.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".zip");
            }
        });
        HashSet<String> files = new HashSet<String>(Arrays.asList(fileArray));
        Session sess = null;
        try {
            sess = library.openSession();
            List<FileEntity> libFiles = sess.createQuery("from FileEntity order by name").list();
            for (FileEntity file : libFiles) {
                if (!files.contains(file.getName())) {
                    newFiles.add(file.getName());
                }
            }
        } catch (HibernateException ex) {
            if (sess != null) sess.close();
            return null;
        }
        return newFiles;
    }

    /**
     * см. @ru.lsv.lib.library.LibraryRealization
     *
     * @param fileListener Листенер при обработке файлов. См {@link ru.lsv.lib.parsers.FileParserListener}
     * @param diffListener Листенер при обработке архивов. См. {@link ru.lsv.lib.library.LibraryDiffListener}
     * @return см. {@link ru.lsv.lib.library.LibraryRealization}
     */
    @Override
    public int processNewBooks(LibraryDiffListener diffListener, FileParserListener fileListener) {
        // Поехали получать дифф
        List<String> newFiles = getFilesDiff();
        if (newFiles == null)
            return -1;
        Library library = LibraryStorage.getSelectedLibrary();
        if (newFiles.size() == 0) return 0;// Ну а чего - разве не нормально прочиталось :)?
        FB2ZipFileParser zipParser = new FB2ZipFileParser();
        // TODO Переделать листенера для отображения ОБЩЕГО процесса! А то сейчас будет отображаться только процесс внутри ОДНОГО зипа
        // TODO Тут же подумать - хорошо бы как-то сообщать список обработанных зипов. Или с вышенаписанным - сделать листенера для fail'ов
        zipParser.addListener(fileListener);
        if (diffListener != null) diffListener.totalFilesInDiffCounted(newFiles.size());
        boolean hasFailed = false;
        for (String file : newFiles) {
            try {
                if (diffListener != null) diffListener.beginNewFile(file);
                // Формируем список книг
                List<Book> books = zipParser.parseZipFile(library.getStoragePath() + File.separatorChar + file);
                // Сохраняем
                Session sess = library.openSession();
                if (sess == null) return -2;
                Transaction trx = null;
                try {
                    trx = sess.beginTransaction();
                    for (Book book : books) sess.save(book);
                    sess.flush();
                    trx.commit();
                } catch (HibernateException e) {
                    if (trx != null) trx.rollback();
                    hasFailed = true;
                } finally {
                    sess.close();
                }
            } catch (Exception e) {
                // TODO Вот тут должен быть вызов листенера с тем, что все сдохло нафиг
            }
        }
        return hasFailed ? -2 : 0;
    }
}
