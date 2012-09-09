package ru.lsv.lib.library.librusec;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.lsv.lib.common.Book;
import ru.lsv.lib.common.FileEntity;
import ru.lsv.lib.library.*;
import ru.lsv.lib.parsers.FB2ZipFileParser;
import ru.lsv.lib.parsers.FileParserListener;
import ru.lsv.lib.parsers.INPRecord;
import ru.lsv.lib.parsers.INPXParser;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

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
        //TreeSet<String> files = new TreeSet<String>(Arrays.asList(fileArray));
        Session sess = null;
        try {
            sess = library.openSession();
            TreeSet<String> libFiles = new TreeSet<String>(sess.createQuery("select name from FileEntity order by name").list());
            if (libFiles.size() == 0) {
                newFiles.addAll(Arrays.asList(fileArray));
            } else {
                for (String file : fileArray) {
                    if (!libFiles.contains(file)) {
                        newFiles.add(file);
                    }
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
        // fire listener
        if (diffListener != null) diffListener.totalFilesInDiffCounted(newFiles.size());
        //
        zipParser.addListener(fileListener);
        boolean hasFailed = false;
        Map<String, INPRecord> inpRecords = null;
        if ((LibraryStorage.getSelectedLibrary().getInpxPath() != null) &&
                (LibraryStorage.getSelectedLibrary().getInpxPath().length() > 0)) {
            // Пробуем загрузить INPX-файл
            try {
                inpRecords = new INPXParser(LibraryStorage.getSelectedLibrary().getInpxPath()).getRecords();
            } catch (IOException e) {
                inpRecords = null;
            }
        }
        for (String file : newFiles) {
            try {
                // fire listener
                if (diffListener != null) diffListener.beginNewFile(file);
                // Формируем список книг
                File fl = new File(library.getStoragePath() + File.separatorChar + file);
                List<Book> books = zipParser.parseZipFile(library.getStoragePath() + File.separatorChar + file, inpRecords);
                // Сохраняем
                // Дата и время добавления. Для одного диффа - оно одинаково
                if (diffListener != null) diffListener.fileProcessSavingBooks(file);
                Date addDate = new Date();
                for (Book book : books) {
                    // Установим дату обновления - и сохраним
                    book.setAddTime(addDate);
                    // Добавляем в библиотеку
                    LibraryUtils.addBookToLibrary(book);
                }
                // Все обработалось. Надо бы, наверное, и файл в library сохранить
                Session sess = library.openSession();
                Transaction trx = null;
                try {
                    trx = sess.beginTransaction();
                    sess.save(new FileEntity(file, fl.length()));
                    sess.flush();
                    trx.commit();
                } catch (HibernateException ex) {
                    if (trx != null) trx.rollback();
                } finally {
                    sess.close();
                }
            } catch (Exception e) {
                if (diffListener != null) diffListener.fileProcessFailed(file, e.getMessage());
            }
        }
        return hasFailed ? -2 : 0;
    }
}
