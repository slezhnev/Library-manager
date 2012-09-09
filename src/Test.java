import org.hibernate.Session;
import ru.lsv.lib.common.Author;
import ru.lsv.lib.common.Book;
import ru.lsv.lib.library.Library;
import ru.lsv.lib.library.LibraryDiffListener;
import ru.lsv.lib.library.LibraryStorage;
import ru.lsv.lib.library.LibraryUtils;
import ru.lsv.lib.parsers.FileParserListener;
import ru.lsv.lib.parsers.INPRecord;
import ru.lsv.lib.parsers.INPXParser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: admin
 * Date: 13.10.2010
 * Time: 16:20:10
 * To change this template use File | Settings | File Templates.
 */
public class Test {

    public static void main(String[] args) throws IOException, INPRecord.BadINPRecord {

        INPXParser inpxp = new INPXParser("I:\\Torrents\\Lib.Rus.Ec + MyHomeLib[FB2]\\librusec_local_fb2.inpx" );

        // А поедем напечатаем!
        for (String name : inpxp.getRecords().keySet()) {
            System.out.println("key: " + name + ", val: " + inpxp.getRecords().get(name));
        }
    }

}
