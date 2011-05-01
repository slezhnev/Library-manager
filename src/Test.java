import org.hibernate.Session;
import ru.lsv.lib.common.Author;
import ru.lsv.lib.common.Book;
import ru.lsv.lib.library.Library;
import ru.lsv.lib.library.LibraryDiffListener;
import ru.lsv.lib.library.LibraryStorage;
import ru.lsv.lib.library.LibraryUtils;
import ru.lsv.lib.parsers.FileParserListener;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: admin
 * Date: 13.10.2010
 * Time: 16:20:10
 * To change this template use File | Settings | File Templates.
 */
public class Test {

    public static void main(String[] args) {

        // Создадим Library одну для теста
        //Library library = new Library("LibRusEc local", "h:/javaprojects/librarian/test.lib", "h:/javaprojects/librarian/librusec.db", 1);
        //System.out.println("add result - " + LibraryStorage.addLibrary(library) + "\n");

        List<Library> libraries = LibraryStorage.getLibraries();
        for (Library library1 : libraries) {
            System.out.println("" + library1 + "\n");
        }

        if (LibraryStorage.selectLibrary(LibraryStorage.getLibraries().get(0).getLibraryId()) != 0) {
            System.out.println("Library select failed!\n");
            return;
        }

        System.out.println("new books? : "+LibraryStorage.getSelectedLibrary().getLibraryRealization().IsNewBooksPresent()+"\n");

        Author tmp = new Author();
        tmp.setLastName("Панов");
        tmp.setFirstName("Вадим");
        tmp = LibraryUtils.getAuthorFromDB(tmp);

        LibraryStorage.shutdownSelectedLibrary();
        LibraryStorage.shutdownStorage();        
    }

}
