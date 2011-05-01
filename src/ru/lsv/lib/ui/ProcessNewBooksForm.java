package ru.lsv.lib.ui;

import ru.lsv.lib.common.Book;
import ru.lsv.lib.library.Library;
import ru.lsv.lib.library.LibraryDiffListener;
import ru.lsv.lib.library.LibraryStorage;
import ru.lsv.lib.parsers.FileParserListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        } else {
            if (LibraryStorage.getSelectedLibrary().getLibraryRealization().IsNewBooksPresent() == 1) {
                infoLabel.setText("В библиотеке " + LibraryStorage.getSelectedLibrary().getName() + " присутствуют новые книги");
            } else {
                loadNewBooksBtn.setEnabled(false);
            }
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
        dialog.setVisible(true);
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
