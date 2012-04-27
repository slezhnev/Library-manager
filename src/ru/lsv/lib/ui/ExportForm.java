package ru.lsv.lib.ui;

import ru.lsv.lib.common.Book;
import ru.lsv.lib.library.Library;
import ru.lsv.lib.library.LibraryStorage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Форма экспорта книг из библиотеки
 */
public class ExportForm {

    private JDialog dialog;

    private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
    private Cursor defCursor = new Cursor(Cursor.DEFAULT_CURSOR);

    private JPanel mainPanel;
    private JTextField exportLocationEdit;
    private JButton button1;
    private JLabel infoLabel;
    private JButton exportBtn;
    private JCheckBox markAsReadedCB;
    private JButton closeBtn;
    private JProgressBar progressBar1;

    private boolean modalResult = false;

    public ExportForm(Frame owner, final List<Book> books) {
        dialog = new JDialog(owner, "Экспорт книг");
        dialog.setModal(true);
        dialog.getContentPane().add(mainPanel);
        dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //
        if (LibraryStorage.getSelectedLibrary() == null) {
            infoLabel.setText("Не выбрана библиотека для работы!");
        } else {
            infoLabel.setText("Всего книг для экспорта - " + books.size());
        }
        //
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setDialogTitle("Выберите место для сохранения");
                if (chooser.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
                    exportLocationEdit.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        });
        exportBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Вначале - проверим куда мы там хотим экспортировать
                if (exportLocationEdit.getText().trim().length() == 0) {
                    JOptionPane.showMessageDialog(mainPanel, "Не указано место экспорта", "Экспорт книг", JOptionPane.WARNING_MESSAGE);
                } else {
                    File file = new File(exportLocationEdit.getText());
                    if (!file.exists()) {
                        JOptionPane.showMessageDialog(mainPanel, "Место экспорта не существует", "Экспорт книг", JOptionPane.WARNING_MESSAGE);
                    } else if (!file.isDirectory()) {
                        JOptionPane.showMessageDialog(mainPanel, "Место экспорта - не каталог", "Экспорт книг", JOptionPane.WARNING_MESSAGE);
                    } else {
                        performExport(books);
                    }
                }
            }
        });
        // Загружаем путь экспорта
        Properties props = new Properties();
        try {
            props.load(new FileReader("export.properties"));
            exportLocationEdit.setText(props.getProperty("export.location"));
            markAsReadedCB.setSelected("true".equals(props.getProperty("export.markAsRead")));
        } catch (IOException ignored) {
        }
    }

    /**
     * Непосредственно переносит книги. Куда переносить - указывается в exportLocationEdit
     *
     * @param books Список книг для экспорта
     */
    private void performExport(final List<Book> books) {
        mainPanel.setCursor(waitCursor);
        progressBar1.setMaximum(books.size());
        progressBar1.setValue(0);
        progressBar1.setString("");
        exportBtn.setText("Идет экспорт...");
        exportBtn.setEnabled(false);
        closeBtn.setEnabled(false);
        final Library library = LibraryStorage.getSelectedLibrary();
        new Thread(new Runnable() {
            /**
             * Включает контролы при ошибке обработки
             */
            private void doEnableOnError() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        exportBtn.setText("Экспортировать");
                        exportBtn.setEnabled(true);
                        closeBtn.setEnabled(true);
                        mainPanel.setCursor(defCursor);
                    }
                });
            }

            @Override
            public void run() {
                //
                for (final Book book : books) {
                    // Выдаем название
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            progressBar1.setString(book.toString());
                        }
                    });
                    // Экспортируем
                    // Первое - проверяем, а есть ли архив с файлом
                    File arcFile = new File(library.getStoragePath() + File.separator + book.getZipFileName());
                    if (!arcFile.exists()) {
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                @Override
                                public void run() {
                                    JOptionPane.showMessageDialog(mainPanel, "Не найден файл " + library.getStoragePath() + File.separator + book.getZipFileName() + "! Экспорт остановлен",
                                            "Экспорт книг", JOptionPane.ERROR_MESSAGE);
                                }
                            });
                        } catch (InterruptedException ignored) {
                        } catch (InvocationTargetException ignored) {
                        }
                        doEnableOnError();
                        return;
                    } else {
                        // Создаем место - куда сохранять...
                        final StringBuffer str = new StringBuffer(exportLocationEdit.getText() + File.separator + book.getAuthorsToString());
                        if (book.getSerieName() != null) {
                            str.append(File.separator).append(book.getSerieName().trim());
                        }
                        File outFile = new File(str.toString());
                        if ((!outFile.exists()) && (!outFile.mkdirs())) {
                            try {
                                SwingUtilities.invokeAndWait(new Runnable() {
                                    @Override
                                    public void run() {
                                        JOptionPane.showMessageDialog(mainPanel, "Ошибка создания пути экспорта \"" + str.toString() + "\"! Экспорт остановлен",
                                                "Экспорт книг", JOptionPane.ERROR_MESSAGE);
                                    }
                                });
                            } catch (InterruptedException ignored) {
                            } catch (InvocationTargetException ignored) {
                            }
                            doEnableOnError();
                            return;
                        }
                        // Проверяем на наличие такого файла
                        str.append(File.separator).append(book.toString().trim()).append(".fb2.zip");
                        outFile = new File(str.toString());
                        if (outFile.exists() && (!outFile.delete())) {
                            try {
                                SwingUtilities.invokeAndWait(new Runnable() {
                                    @Override
                                    public void run() {
                                        JOptionPane.showMessageDialog(mainPanel, "Файл " + str.toString() + " уже существует и не хочет удаляться! Экспорт остановлен",
                                                "Экспорт книг", JOptionPane.ERROR_MESSAGE);
                                    }
                                });
                            } catch (InterruptedException ignored) {
                            } catch (InvocationTargetException ignored) {
                            }
                            doEnableOnError();
                            return;
                        }
                        // Сохраняем...
                        try {
                            ZipFile zip = new ZipFile(arcFile);
                            for (Enumeration e = zip.entries(); e.hasMoreElements();) {
                                ZipEntry ze = (ZipEntry) e.nextElement();
                                String name = ze.getName();
                                String id = name.substring(0, name.indexOf("."));
                                if (id.equals(book.getId())) {
                                    // Во - нашли книгу. Поехали ее доставать куда-нито
                                    InputStream in = zip.getInputStream(ze);
                                    // Во! Даже стрим есть!
                                    if (!outFile.createNewFile()) {
                                        try {
                                            final File finalOutFile = outFile;
                                            SwingUtilities.invokeAndWait(new Runnable() {
                                                @Override
                                                public void run() {
                                                    JOptionPane.showMessageDialog(mainPanel, "Ошибка создания файла " + finalOutFile.getAbsolutePath() + "! Экспорт остановлен",
                                                            "Экспорт книг", JOptionPane.ERROR_MESSAGE);
                                                }
                                            });
                                        } catch (InterruptedException ignored) {
                                        } catch (InvocationTargetException ignored) {
                                        }
                                        doEnableOnError();
                                        return;
                                    } else {
                                        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile));
                                        out.setLevel(9);
                                        ZipEntry outEntry = new ZipEntry(book.getId() + ".fb2");
                                        out.putNextEntry(outEntry);
                                        byte[] buffer = new byte[100000];
                                        while (true) {
                                            int amountRead = in.read(buffer);
                                            if (amountRead == -1) {
                                                break;
                                            }
                                            out.write(buffer, 0, amountRead);
                                        }
                                        in.close();
                                        out.close();
                                        break;
                                    }
                                }
                            }
                        } catch (IOException e) {
                            try {
                                SwingUtilities.invokeAndWait(new Runnable() {
                                    @Override
                                    public void run() {
                                        JOptionPane.showMessageDialog(mainPanel, "Ошибка обработки файла " + book.getZipFileName() + "! Экспорт остановлен",
                                                "Экспорт книг", JOptionPane.ERROR_MESSAGE);
                                    }
                                });
                            } catch (InterruptedException ignored) {
                            } catch (InvocationTargetException ignored) {
                            }
                            doEnableOnError();
                            return;
                        }
                    }
                    //
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            progressBar1.setValue(progressBar1.getValue() + 1);
                        }
                    });
                }
                // Если мы сюда попали - то все книги успешно сохранились. Значит все успешно завершаем
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        modalResult = true;
                        dialog.setVisible(false);
                    }
                });
            }
        }).start();
    }

    /**
     * Отобразить диалог экспорта
     *
     * @param positionComponent Компонент для позиционирования
     * @return -1 - закрыт без экспорта, 0 - все экспортировано, 1 - все экспортировано, но надо все отметить как прочитанное
     */
    public int doExport(Component positionComponent) {
        dialog.pack();
        dialog.setBounds(0, 0, 800, dialog.getHeight());
        dialog.setLocationRelativeTo(positionComponent);
        dialog.setVisible(true);
        Properties props = new Properties();
        props.setProperty("export.location", exportLocationEdit.getText());
        props.setProperty("export.markAsRead", ""+markAsReadedCB.isSelected());
        try {
            props.store(new FileWriter("export.properties"), "");
        } catch (IOException ignored) {
        }
        if (modalResult) {
            if (markAsReadedCB.isSelected()) return 1;
            else return 0;
        } else return -1;
    }
}
