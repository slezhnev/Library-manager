import org.hibernate.Session;
import ru.lsv.lib.common.Book;
import ru.lsv.lib.library.Library;
import ru.lsv.lib.library.LibraryDiffListener;
import ru.lsv.lib.library.LibraryStorage;
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
        Library library = new Library("LibRusEc local", "h:/javaprojects/librarian/test.lib", "h:/javaprojects/librarian/librusec.db", 1);
        System.out.println("add result - " + LibraryStorage.addLibrary(library) + "\n");

        List<Library> libraries = LibraryStorage.getLibraries();
        for (Library library1 : libraries) {
            System.out.println("" + library1 + "\n");
        }

        if (LibraryStorage.selectLibrary(LibraryStorage.getLibraries().get(0).getLibraryId()) != 0) {
            System.out.println("Library select failed!\n");
            return;
        }

        System.out.println("new books? : "+LibraryStorage.getSelectedLibrary().getLibraryRealization().IsNewBooksPresent()+"\n");
        if (LibraryStorage.getSelectedLibrary().getLibraryRealization().IsNewBooksPresent() == 1) {
            System.out.println("res of processing new : " + LibraryStorage.getSelectedLibrary().getLibraryRealization().processNewBooks(
                    new LibraryDiffListener() {
                        @Override
                        public void totalFilesInDiffCounted(int totalFilesInDiff) {
                            System.out.println ("total files in diff :"+totalFilesInDiff+"\n");
                        }

                        @Override
                        public void beginNewFile(String fileName) {
                            System.out.println("- processing new file : "+fileName+"\n");
                        }

                        @Override
                        public void fileProcessFailed(String fileName, String msg) {
                            System.out.println("- FAILED to process file : "+fileName+", reason - "+msg+"\n");
                        }
                    },
                    new FileParserListener() {
                        @Override
                        public void inArchiveFilesCounted(int numFilesInZip) {
                            System.out.println("-- files in archive : "+numFilesInZip+"\n");
                        }

                        @Override
                        public void inArchiveFileProcessed(String fileName, Book book) {
                            System.out.println("--- processed in file "+fileName+" book - "+book);
                        }
                    }
            ) + "\n");
        }

    }

}
