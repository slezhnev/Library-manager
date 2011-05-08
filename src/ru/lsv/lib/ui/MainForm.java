package ru.lsv.lib.ui;

import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.lsv.lib.common.Author;
import ru.lsv.lib.common.Book;
import ru.lsv.lib.library.Library;
import ru.lsv.lib.library.LibraryStorage;
import ru.lsv.lib.parsers.MHLUDParser;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Основная форма приложения
 * User: Lsv
 * Date: 06.11.2010
 * Time: 17:18:52
 */
public class MainForm implements ActionListener {

    private JFrame mainFrame;

    private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
    private Cursor defCursor = new Cursor(Cursor.DEFAULT_CURSOR);

    private JPanel mainPanel;
    private JTabbedPane filterTypeTB;
    private JTextField authorEdit;
    private JList authorsList;
    private JTree booksTree;
    private JList seriesList;
    private JTextField seriesEdit;
    private JLabel bookDescription;
    private JCheckBoxMenuItem filterOnlyNewMI;
    private JCheckBoxMenuItem filterOnlyMustReadMI;
    private static final String checkNewBooksMIText = "Проверить обновление";
    private static final String closeMIText = "Завершить работу";
    private static final String loadReadedListFromMyHomeLibMIText = "Загрузить список прочитанного из MyHomeLib";
    private static final String markAsReadedMIText = "Отметить/снять отметку 'Прочитанное'";
    private static final String markAsMustReadMIText = "Отметить/снять отметку 'К прочтению'";
    private static final String copyToDeviceMIText = "Копировать для чтения";

    public MainForm() {
        authorEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterByCategory();
            }
        });
        authorsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                filterBooks();
            }
        });
        seriesEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterByCategory();
            }
        });
        seriesList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                filterBooks();
            }
        });
        filterTypeTB.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                filterBooks();
            }
        });
        booksTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (e.getNewLeadSelectionPath() != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getNewLeadSelectionPath().getLastPathComponent();
                    if (node.getUserObject().getClass().equals(Book.class))
                        formBookDescription((Book) node.getUserObject());
                }
            }
        });
    }

    /**
     * Фильтрация списка авторов, серий или книг - в зависимости от того, чо щас выбрано
     */
    private void filterByCategory() {
        switch (filterTypeTB.getSelectedIndex()) {
            case 0: {
                filterAuthors();
                break;
            }
            case 1: {
                filterSeries();
                break;
            }
        }
    }

    /**
     * Формирует описание книги
     *
     * @param book Книга, для которой формировать описание
     */
    private void formBookDescription(Book book) {
        if (LibraryStorage.getSelectedLibrary() != null) {
            mainFrame.setCursor(waitCursor);
            Session sess = null;
            try {
                sess = LibraryStorage.getSelectedLibrary().openSession();
                book = (Book) sess.get(Book.class, book.getBookId());
                bookDescription.setText(book.formHTMLDescription());
            } finally {
                if (sess != null) sess.close();
                mainFrame.setCursor(defCursor);
            }
        }
    }

    /**
     * Фильтрация списка серий
     */
    private void filterSeries() {
        mainFrame.setCursor(waitCursor);
        // Если библиотека не выбрана - ничего не делаем
        if (LibraryStorage.getSelectedLibrary() == null) return;
        Session sess = null;
        try {
            sess = LibraryStorage.getSelectedLibrary().openSession();
            List<String> series;
            if (filterOnlyNewMI.isSelected() && filterOnlyMustReadMI.isSelected()) {
                series = sess.createQuery("select DISTINCT serieName from Book where serieName LIKE ? AND mustRead=true and (readed=false or readed is null) AND (serieName in (select DISTINCT serieName from Book where readed=true)) order by serieName").
                        setString(0, seriesEdit.getText() + "%").list();
            } else if (filterOnlyNewMI.isSelected()) {
                series = sess.createQuery("select DISTINCT serieName from Book where serieName LIKE ? AND (readed=false or readed is null) AND (serieName in (select DISTINCT serieName from Book where readed=true)) order by serieName").
                        setString(0, seriesEdit.getText() + "%").list();
            } else if (filterOnlyMustReadMI.isSelected()) {
                series = sess.createQuery("select DISTINCT serieName from Book where serieName LIKE ? AND mustRead=true order by serieName").
                        setString(0, seriesEdit.getText() + "%").list();
            } else {
                series = sess.createQuery("select DISTINCT serieName from Book where serieName LIKE ? order by serieName").
                        setString(0, seriesEdit.getText() + "%").list();
            }
            ((StringsListModel) seriesList.getModel()).setStrings(series);
            seriesList.clearSelection();
        } finally {
            if (sess != null) sess.close();
            mainFrame.setCursor(defCursor);
        }
    }

    /**
     * Фильтрация списка авторов
     */
    private void filterAuthors() {
        mainFrame.setCursor(waitCursor);
        // Если библиотека не выбрана - ничего не делаем
        if (LibraryStorage.getSelectedLibrary() == null) return;
        Session sess = null;
        try {
            sess = LibraryStorage.getSelectedLibrary().openSession();
            List<Author> authors = sess.createQuery("from Author where lastName LIKE ? order by lastName, firstName, middleName").
                    setString(0, authorEdit.getText() + "%").list();
            ((AuthorListModel) authorsList.getModel()).setAuthors(authors);
            authorsList.clearSelection();
        } finally {
            if (sess != null) sess.close();
            mainFrame.setCursor(defCursor);
        }
    }

    /**
     * Формирует список книг в booksTree
     */
    private void filterBooks() {
        mainFrame.setCursor(waitCursor);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        switch (filterTypeTB.getSelectedIndex()) {
            case 0: {
                if (authorsList.getSelectedIndex() != -1) {
                    Session sess = null;
                    try {
                        sess = LibraryStorage.getSelectedLibrary().openSession();
                        // Загружаем автора полностью
                        //Author author = (Author) sess.get(Author.class, ((AuthorListModel) authorsList.getModel()).get(authorsList.getSelectedIndex()).getAuthorId());
                        Author author = (Author) sess.load(Author.class, ((AuthorListModel) authorsList.getModel()).get(authorsList.getSelectedIndex()).getAuthorId());
                        if (author.getBooks().size() > 0) {
                            fillBookTree(root, author.getBooks());
                        }
                    } finally {
                        if (sess != null) sess.close();
                    }
                }
                break;
            }
            case 1: {
                if (seriesList.getSelectedIndex() != -1) {
                    Session sess = null;
                    try {
                        sess = LibraryStorage.getSelectedLibrary().openSession();
                        List<Book> books = sess.createQuery("from Book where serieName=? order by numInSerie").
                                setString(0, (String) seriesList.getModel().getElementAt(seriesList.getSelectedIndex())).
                                list();
                        if (books.size() > 0) {
                            fillBookTree(root, books);
                        }
                    } finally {
                        if (sess != null) sess.close();
                    }
                }
                break;
            }
        }
        ((DefaultTreeModel) booksTree.getModel()).setRoot(root);
        ((DefaultTreeModel) booksTree.getModel()).reload();
        booksTree.setSelectionPaths(null);
        for (int i = 0; i < booksTree.getRowCount(); i++) {
            booksTree.expandRow(i);
        }
        mainFrame.setCursor(defCursor);
    }


    /**
     * Заполняет список книг в дереве
     *
     * @param root  Корень дерева
     * @param books Список книг
     */
    private void fillBookTree(DefaultMutableTreeNode root, List<Book> books) {
        // Сортируем - чтобы совсем не ломать hibernate mapping
        TreeSet<Book> sortedBooks = new TreeSet<Book>(new BookComparator());
        sortedBooks.addAll(books);
        DefaultMutableTreeNode serieNode = null;
        for (Book book : sortedBooks) {
            if ((book.getSerieName() != null) && (book.getSerieName().trim().length() > 0)) {
                // Есть серия!
                if ((serieNode == null) || (!book.getSerieName().equals(serieNode.getUserObject()))) {
                    serieNode = new DefaultMutableTreeNode(book.getSerieName());
                    root.add(serieNode);
                }
            } else {
                // Серии нет - сбрасываем вершину
                serieNode = null;
            }
            DefaultMutableTreeNode bookNode = new DefaultMutableTreeNode(book);
            if (serieNode != null) serieNode.add(bookNode);
            else root.add(bookNode);
        }
    }


    public JPanel buildMainPanel() {
        return mainPanel;
    }

    public JFrame buildMainFrame() {
        mainFrame = new JFrame(getDefaultTitle());
        mainFrame.setJMenuBar(buildMainMenu());
        mainFrame.setContentPane(buildMainPanel());
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(null, "Завершить работу с системой?", "Завершение работы", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    doExit();
                }
            }
        });
        //
        authorsList.setModel(new AuthorListModel());
        seriesList.setModel(new StringsListModel());
        //
        //mainFrame.pack();
        mainFrame.setBounds(0, 0, 1024, 800);
        mainFrame.setLocationRelativeTo(null);
        //
        initLibrary();
        //filterAuthors();
        //filterBooks();
        booksTree.setCellRenderer(new BookTreeCellRenderer());
        ((DefaultTreeModel) booksTree.getModel()).setRoot(new DefaultMutableTreeNode("root"));
        booksTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        //
        mainFrame.setVisible(true);
        return mainFrame;
    }


    /**
     * Получение заголовка фрейма по умолчанию
     *
     * @return Заголовок фрейма по умолчанию
     */
    private String getDefaultTitle() {
        return "Librarian";
    }

    /**
     * Инициализация работы с библиотекой
     */
    private void initLibrary() {
        List<Library> libraries = LibraryStorage.getLibraries();
        if (libraries.size() == 1) {
            // Выбираем первую библиотеку
            doLibrarySelection(libraries.get(0));
        } else if (libraries.size() == 0) {
            // Добавляем библиотеку
            Library library = new Library("LibRusEc",
                    "I:/Torrents/Lib.Rus.Ec + MyHomeLib[FB2]/lib.rus.ec",
                    "I:/Torrents/Lib.Rus.Ec + MyHomeLib[FB2]/librarian.data/librusec.db", 1);
            LibraryStorage.addLibrary(library);
            doLibrarySelection(library);
        }
    }

    /**
     * Выбор библиотеки для работы
     *
     * @param library Библиотека для работы
     */
    private void doLibrarySelection(Library library) {
        mainFrame.setTitle(getDefaultTitle() + " - " + library.getName());
        LibraryStorage.selectLibrary(library.getLibraryId());
    }

    /**
     * Завершает работу с системой
     */
    private void doExit() {
        if (LibraryStorage.getSelectedLibrary() != null)
            LibraryStorage.shutdownSelectedLibrary();
        LibraryStorage.shutdownStorage();
        System.exit(0);
    }

    public JMenuBar buildMainMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Библиотеки");
        JMenuItem item;
        /*item = new JMenuItem("Создать новую...");
        item.addActionListener(this);
        menu.add(item);
        item = new JMenuItem("Открыть");
        item.addActionListener(this);
        menu.add(item);
        item = new JMenuItem("Параметры текущей");
        item.addActionListener(this);
        menu.add(item);
        menu.addSeparator();*/
        item = new JMenuItem(checkNewBooksMIText);
        item.addActionListener(this);
        menu.add(item);
        //
        menu.addSeparator();
        item = new JMenuItem(loadReadedListFromMyHomeLibMIText);
        item.addActionListener(this);
        menu.add(item);
        //
        menu.addSeparator();
        item = new JMenuItem(closeMIText);
        item.addActionListener(this);
        menu.add(item);
        //
        menuBar.add(menu);
        //
        menu = new JMenu("Параметры фильтрации");
        filterOnlyNewMI = new JCheckBoxMenuItem("Показывать только новое");
        filterOnlyNewMI.addActionListener(this);
        menu.add(filterOnlyNewMI);
        filterOnlyMustReadMI = new JCheckBoxMenuItem("Показывать только 'К прочтению'");
        filterOnlyMustReadMI.addActionListener(this);
        menu.add(filterOnlyMustReadMI);
        //
        menuBar.add(menu);
        //
        menu = new JMenu("Действия");
        item = new JMenuItem(markAsReadedMIText);
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl R"));
        item.addActionListener(this);
        menu.add(item);
        item = new JMenuItem(markAsMustReadMIText);
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl P"));
        item.addActionListener(this);
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem(copyToDeviceMIText);
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl D"));
        item.addActionListener(this);
        menu.add(item);
        //
        menuBar.add(menu);
        //
        return menuBar;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (closeMIText.equals(e.getActionCommand())) {
            if (JOptionPane.showConfirmDialog(null, "Завершить работу с системой?", "Завершение работы", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                doExit();
            }
        } else if (checkNewBooksMIText.equals(e.getActionCommand())) {
            doNewBookCheck();
        } else if (loadReadedListFromMyHomeLibMIText.equals(e.getActionCommand())) {
            doLoadReadedFromMyHomeLib();
        } else if (markAsReadedMIText.equals(e.getActionCommand())) {
            doMarkAsRead(true);
        } else if (markAsMustReadMIText.equals(e.getActionCommand())) {
            doMarkAsMustRead();
        } else if ((filterOnlyNewMI.getText().equals(e.getActionCommand())) ||
                (filterOnlyMustReadMI.getText().equals(e.getActionCommand()))) {
            filterByCategory();
        } else if (copyToDeviceMIText.equals(e.getActionCommand())) {
            doCopyToDevice();
        }
    }

    /**
     * Копирует выбранные книги куда-нито
     * <p/>
     * Формат копирования - fb2.zip
     * Куда копировать - будет спрошено
     * Дополнительно будет спрошено - отмечать ли скопированные книги как прочитанные
     */
    private void doCopyToDevice() {
        if (LibraryStorage.getSelectedLibrary() == null) {
            JOptionPane.showMessageDialog(mainPanel, "Не выбрана библиотека для работы", "Экспорт книг", JOptionPane.ERROR_MESSAGE);
        } else {
            TreePath[] selection = booksTree.getSelectionPaths();
            if ((selection != null) && (selection.length > 0)) {
                // Сформируем список книг для экспорта
                Session sess = null;
                mainPanel.setCursor(waitCursor);
                ArrayList<Book> books = new ArrayList<Book>();
                try {
                    sess = LibraryStorage.getSelectedLibrary().openSession();
                    for (TreePath el : selection) {
                        if (DefaultMutableTreeNode.class.equals(el.getLastPathComponent().getClass())) {
                            if (Book.class.equals(((DefaultMutableTreeNode) el.getLastPathComponent()).getUserObject().getClass())) {
                                // Загрузим книгу полностью!
                                Book book = (Book) sess.get(Book.class, ((Book) ((DefaultMutableTreeNode) el.getLastPathComponent()).getUserObject()).getBookId());
                                if (book.getAuthors() != null) {
                                    int makeLazyInitialization = book.getAuthors().size();
                                }
                                books.add(book);
                            }
                        }
                    }
                } finally {
                    sess.close();
                    mainPanel.setCursor(defCursor);
                }
                ExportForm export = new ExportForm(mainFrame, books);
                if (export.doExport(mainPanel) == 1) {
                    doMarkAsRead(false);
                }
            }
        }
    }

    /**
     * Поставить или снять отметку "К прочтению"
     */
    private void doMarkAsMustRead() {
        doMark(1, true);
    }

    /**
     * Отметить или снять отметку "Прочитано"
     *
     * @param allowRemove Разрешать или нет снятие отметки
     */
    private void doMarkAsRead(boolean allowRemove) {
        doMark(0, allowRemove);
    }

    /**
     * Снятие или установка отметки
     *
     * @param whatMark    0 - readed, 1 - mustRead
     * @param allowRemove Разрешать или нет снятие отметки
     */
    private void doMark(int whatMark, boolean allowRemove) {
        TreePath[] selection = booksTree.getSelectionPaths();
        if ((selection != null) && (selection.length > 0)) {
            // Поехали обрабатывать...
            Session sess = null;
            Transaction trx = null;
            try {
                mainPanel.setCursor(waitCursor);
                sess = LibraryStorage.getSelectedLibrary().openSession();
                trx = sess.beginTransaction();
                for (TreePath el : selection) {
                    if (DefaultMutableTreeNode.class.equals(el.getLastPathComponent().getClass())) {
                        // Значит тут - вершина...
                        if (Book.class.equals(((DefaultMutableTreeNode) el.getLastPathComponent()).getUserObject().getClass())) {
                            // Значит - это еще и книга. Тогда точно поехали обрабатывать...
                            Book book = (Book) ((DefaultMutableTreeNode) el.getLastPathComponent()).getUserObject();
                            switch (whatMark) {
                                case 0: {
                                    if ((book.getReaded() == null) || (!book.getReaded())) book.setReaded(true);
                                    else if (allowRemove) book.setReaded(false);
                                    break;
                                }
                                case 1: {
                                    if ((book.getMustRead() == null) || (!book.getMustRead())) book.setMustRead(true);
                                    else if (allowRemove) book.setMustRead(false);
                                    break;
                                }
                            }
                            sess.update(book);
                            ((DefaultTreeModel) booksTree.getModel()).nodeChanged((TreeNode) el.getLastPathComponent());
                        }
                    }
                }
                sess.flush();
                trx.commit();
                trx = null;
                sess.close();
                sess = null;
            } finally {
                if (trx != null) trx.rollback();
                if (sess != null) sess.close();
                mainPanel.setCursor(defCursor);
            }
        }
    }

    /**
     * Загрузка списка прочитанного из экспортированного файла MyHomeLib
     */
    private void doLoadReadedFromMyHomeLib() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "MHLUD", "mhlud");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(mainPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            mainPanel.setCursor(waitCursor);
            try {
                if (MHLUDParser.doLoadReaded(chooser.getSelectedFile().getName()) != 0) {
                    mainPanel.setCursor(defCursor);
                    JOptionPane.showMessageDialog(mainPanel, "Ошибка при загрузке списка прочитанного", "Загрузка списка прочитанного",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    mainPanel.setCursor(defCursor);
                    JOptionPane.showMessageDialog(mainPanel, "Список прочитанного загружен. Для просмотра перезапустите программу", "Загрузка списка прочитанного",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException e) {
                mainPanel.setCursor(defCursor);
                JOptionPane.showMessageDialog(mainPanel, "Ошибка при чтении списка прочитанного", "Загрузка списка прочитанного",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Проверить наличие новых книг
     */
    private void doNewBookCheck() {
        if (LibraryStorage.getSelectedLibrary() != null)
            new ProcessNewBooksForm(mainFrame, mainPanel);
        else {
            JOptionPane.showMessageDialog(mainPanel, "Не выбрана библиотека для работы", "Поиск новых книг", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Класс, реализующий модель списка авторов
     */
    private class AuthorListModel extends AbstractListModel {

        private List<Author> authors = new ArrayList<Author>();

        @Override
        public int getSize() {
            return authors.size();
        }

        @Override
        public Object getElementAt(int index) {
            if (index < getSize()) {
                Author author = authors.get(index);
                return author.makeName();
            } else return null;
        }

        /**
         * Установить новые данные
         *
         * @param authors Список авторов
         */
        public void setAuthors(List<Author> authors) {
            this.authors = authors;
            fireContentsChanged(this, 0, getSize());
        }

        /**
         * Возвращает элемент из списка
         *
         * @param index Номер элемента
         * @return Автор
         */
        public Author get(int index) {
            if (index < getSize()) {
                return authors.get(index);
            } else return null;
        }
    }


    private class StringsListModel extends AbstractListModel {

        List<String> strings = new ArrayList<String>();

        @Override
        public int getSize() {
            return strings.size();
        }

        @Override
        public Object getElementAt(int index) {
            if (index < getSize()) {
                return strings.get(index);
            } else
                return null;
        }

        public void setStrings(List<String> strings) {
            this.strings = strings;
            fireContentsChanged(this, 0, getSize());
        }
    }


    /**
     * Компаратор для сортировки книг
     */
    private static class BookComparator implements Comparator<Book> {
        @Override
        public int compare(Book o1, Book o2) {
            if ((o1.getSerieName() == null) && (o2.getSerieName() != null))
                // У первой нет серии, у второй - есть -> вторая должна быть выше
                return 1;
            else if ((o2.getSerieName() == null) && (o1.getSerieName() != null))
                // У первой - есть серия, у второй - нет -> первая должна быть выше
                return -1;
            else if ((o2.getSerieName() == null) && (o2.getSerieName() == null)) {
                // У обеих серий нет
                if ((o1.getTitle() != null))
                    // Сраниваем по титлу первой
                    if (o1.getTitle().equals(o2.getTitle())) {
                        // Если заголовки совпадают - сравниваем по идентификатору
                        return (new Integer(o1.getId())).compareTo(new Integer(o2.getId()));
                    } else
                        return o1.getTitle().compareTo(o2.getTitle());
                else
                    // У первой титлы нет - значит пусть она будет выше
                    return -1;
            } else {
                // Тут у обеих есть серии
                if (o1.getSerieName().equals(o2.getSerieName())) {
                    // Будем сравнивать по numInSerie
                    if (o1.getNumInSerie() != null) {
                        // У первой есть номер в серии - сравниваем с ним
                        if (o2.getNumInSerie() != null)
                            if (o1.getNumInSerie().equals(o2.getNumInSerie())) {
                                return (new Integer(o1.getId())).compareTo(new Integer(o2.getId()));
                            } else
                                return o1.getNumInSerie().compareTo(o2.getNumInSerie());
                        else
                            // У первой - есть номер серии, у второй - нету -> первая будет ниже
                            return 1;
                    } else
                        // У первой номера серии нету - значит пусть она будет выше
                        return -1;
                } else
                    return o1.getSerieName().compareTo(o2.getSerieName());
            }
        }
    }

    /**
     * Рендерер для элементов дерева книг
     */
    private static class BookTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);
            if ((value != null) && (value.getClass().equals(DefaultMutableTreeNode.class))) {
                Object obj = ((DefaultMutableTreeNode) value).getUserObject();
                if ((obj != null) && (obj.getClass().equals(Book.class))) {
                    Book book = (Book) obj;
                    if ((book.getReaded() != null) && (book.getReaded())) {
                        setText("<html><b>" + book + "</b></html>");
                    } else if ((book.getMustRead() != null) && (book.getMustRead())) {
                        setText("<html><i>" + book + "</i></html>");
                    }
                }
            }
            return this;
        }
    }
}
