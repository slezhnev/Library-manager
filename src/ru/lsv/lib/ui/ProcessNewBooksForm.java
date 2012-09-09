package ru.lsv.lib.ui;

import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
import ru.lsv.lib.common.Author;
import ru.lsv.lib.common.Book;
import ru.lsv.lib.library.Library;
import ru.lsv.lib.library.LibraryDiffListener;
import ru.lsv.lib.library.LibraryStorage;
import ru.lsv.lib.parsers.FileParserListener;
import ru.lsv.lib.parsers.INPRecord;
import ru.lsv.lib.parsers.INPXParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Обработчик новых книг
 */
public class ProcessNewBooksForm {
    private JPanel mainPanel;
    private JButton loadNewBooksBtn;
    private JLabel infoLabel;
    private JProgressBar progressBar1;
    private JProgressBar progressBar2;
    private JList infoList;
    private JScrollPane infoListSP;
    private JButton syncButton;
    private JDialog dialog;

    public ProcessNewBooksForm(Frame owner, Component positionComponent) {
        dialog = new JDialog(owner, "Загрузка новых книг");
        dialog.setModal(true);
        dialog.getContentPane().add(mainPanel);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //
        if (LibraryStorage.getSelectedLibrary() == null) {
            infoLabel.setText("Не выбрана библиотека для работы!");
            loadNewBooksBtn.setEnabled(false);
            syncButton.setEnabled(false);
        } else {
            if (LibraryStorage.getSelectedLibrary().getLibraryRealization().IsNewBooksPresent() == 1) {
                infoLabel.setText("В библиотеке " + LibraryStorage.getSelectedLibrary().getName() + " присутствуют новые книги");
            } else {
                loadNewBooksBtn.setEnabled(false);
            }
            syncButton.setEnabled((LibraryStorage.getSelectedLibrary().getInpxPath() != null) &&
                    (!"".equals(LibraryStorage.getSelectedLibrary().getInpxPath())));
        }
        //
        infoList.setModel(new DefaultListModel());
        //
        dialog.setBounds(0, 0, 800, 600);
        dialog.setLocationRelativeTo(positionComponent);
        loadNewBooksBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doProcessNewBooks();
            }
        });
        syncButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doINPXSync();
            }
        });
        dialog.setVisible(true);
    }

    /**
     * Синхронизация книг с содержимым inpx
     */
    private void doINPXSync() {
        final Library library = LibraryStorage.getSelectedLibrary();
        syncButton.setEnabled(false);
        // Запускаем чтение в другом треде
        new Thread(new Runnable() {
            @Override
            public void run() {
                //
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        addToLog("Загружаем INPX (" + library.getInpxPath() + ")");
                    }
                });
                INPXParser inpxParser = null;
                try {
                    inpxParser = new INPXParser(library.getInpxPath());
                } catch (IOException e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(mainPanel, "Ошибка загрузки INPX файла!", "Синхронизация", JOptionPane.ERROR_MESSAGE);
                            syncButton.setEnabled(true);
                        }
                    });
                }
                if (inpxParser != null) {
                    final Map<String, INPRecord> records = inpxParser.getRecords();
                    // Поехали по записям в inpx
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            infoLabel.setText("Библиотека " + library.getName() + ". Всего файлов в INPX - " + records.size());
                        }
                    });
                    //StatelessSession sess = null;
                    //sess = library.openStatelessSession();
                    Transaction trx = null;
                    StatelessSession sess = null;
                    sess = library.openStatelessSession();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            addToLog("Загружаем книги...");
                        }
                    });
                    final int bookCount = (Integer)sess.createSQLQuery("SELECT COUNT(*) FROM BOOK").uniqueResult(); 
                    final Date startDate = new Date();
                    // Охфигеть сколько оно грузится
                    ScrollableResults books = sess.createQuery("from Book").scroll(ScrollMode.FORWARD_ONLY);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            addToLog("Книги загружены. Всего грузили " + ((new Date().getTime() - startDate.getTime()) / 1000 % 60) + " с.");
                            progressBar1.setMaximum(bookCount);
                        }
                    });
                    while (books.next()) {
                        //
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                progressBar1.setValue(progressBar1.getValue() + 1);
                                /*if (progressBar1.getValue() >= progressBar1.getMaximum()) {
                                    progressBar1.setValue(0);
                                }*/
                            }
                        });
                        //
                        Book book = (Book) books.get(0);
                        final String bookId = book.getId();
                        // Ищем inp record
                        final INPRecord rec = records.get(book.getId());
                        if (rec != null) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    addToLog("Обрабатывается книга с кодом " + bookId);
                                }
                            });
                            // Поехали обрабатывать
                            trx = sess.beginTransaction();
                            // Создаем список авторов
                            book.setAuthors(new ArrayList<Author>());
                            book = rec.fillBookFrom(book);
                            sess.update(book);
                            // Сохраняем авторов
                            // Удаляем вначале
                            sess.createSQLQuery("DELETE FROM BOOK_AUTHORS WHERE BOOK_ID=" + book.getBookId()).executeUpdate();
                            // Формируем добавлятельную квери
                            SQLQuery query = sess.createSQLQuery("INSERT INTO BOOK_AUTHORS VALUES (:bookId, :authId)");
                            // Добавляем
                            for (Author author : book.getAuthors()) {
                                query.setInteger("bookId", book.getBookId()).setInteger("authId", author.getAuthorId()).executeUpdate();
                            }
                            //sess.flush();
                            trx.commit();
                        } else {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    addToLog("В INPX не найдена книга с кодом " + bookId + " - помечаем как удаленную");
                                }
                            });
                            // Помечаем ее как удаленную в библиотеке
                            trx = sess.beginTransaction();
                            book.setDeletedInLibrary(true);
                            sess.update(book);
                            //sess.flush();
                            trx.commit();
                        }
                    }
                    sess.close();
                    //
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(mainPanel, "Синхронизация завершена. Перезапустите приложение", "Синхронизация", JOptionPane.INFORMATION_MESSAGE);
                            syncButton.setEnabled(true);
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * Обработка загрузки новых книг
     */
    private void doProcessNewBooks() {
        loadNewBooksBtn.setEnabled(false);
        final Library library = LibraryStorage.getSelectedLibrary();
        // Запускаем чтение в другом треде
        new Thread(new Runnable() {
            @Override
            public void run() {
                library.getLibraryRealization().processNewBooks(
                        new LibraryDiffListener() {
                            @Override
                            public void totalFilesInDiffCounted(final int totalFilesInDiff) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        infoLabel.setText("Библиотека " + library.getName() + " - найдено " + totalFilesInDiff + " файл(ов)");
                                        //infoLabel.repaint();
                                        progressBar1.setMaximum(totalFilesInDiff);
                                        //progressBar1.repaint();
                                    }
                                });
                            }

                            @Override
                            public void beginNewFile(final String fileName) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar1.setString(fileName + " (" + (progressBar1.getValue() + 1) + "/" + progressBar1.getMaximum() + ")");
                                        progressBar1.setValue(progressBar1.getValue() + 1);
                                        //progressBar1.repaint();
                                        progressBar2.setString("");
                                        progressBar2.setValue(0);
                                        //progressBar2.repaint();
                                    }
                                });
                            }

                            @Override
                            public void fileProcessFailed(final String fileName, final String msg) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        addToLog("! Ошибка обработки " + fileName + ", ошибка - " + msg);
                                    }
                                });
                            }

                            @Override
                            public void fileProcessSavingBooks(final String fileName) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar1.setString(fileName + " (" + (progressBar1.getValue()) + "/" + progressBar1.getMaximum() + ") - идет сохранение книг");
                                    }
                                });
                            }
                        },
                        new FileParserListener() {
                            @Override
                            public void inArchiveFilesCounted(final int numFilesInZip) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar2.setMaximum(numFilesInZip);
                                        //progressBar2.repaint();
                                    }
                                });
                            }

                            @Override
                            public void inArchiveFileProcessed(final String fileName, Book book) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar2.setString(fileName + " (" + (progressBar2.getValue() + 1) + "/" + progressBar2.getMaximum() + ")");
                                        progressBar2.setValue(progressBar2.getValue() + 1);
                                        //progressBar2.repaint();
                                    }
                                });
                            }

                            @Override
                            public void inArchiveFileParseFailed(final String fileName) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        addToLog("! Ошибка парсинга файла " + fileName);
                                    }
                                });
                            }
                        });
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(mainPanel, "Обновление библиотеки завершено", "Загрузка книг", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
        }).start();
    }

    /**
     * Добавление записи в infoList
     *
     * @param msg Имя файла
     */
    private void addToLog(String msg) {
        ((DefaultListModel) infoList.getModel()).addElement(msg);
        infoListSP.getVerticalScrollBar().setValue(infoListSP.getVerticalScrollBar().getMaximum());
    }

}
