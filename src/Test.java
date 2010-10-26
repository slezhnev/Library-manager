import org.hibernate.Session;
import ru.lsv.lib.common.Book;
import ru.lsv.lib.common.HibernateUtil;
import ru.lsv.lib.parsers.FileParserListener;
import ru.lsv.lib.parsers.ZipFileParser;

import java.io.IOException;
import java.text.ParseException;
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

//        Enumeration<Object> keys = FB2Genres.genres.keys();
//
//        while (keys.hasMoreElements()) {
//            System.out.println(""+keys.nextElement());
//        }

        Session sess = HibernateUtil.getSession();
        ZipFileParser zfp = new ZipFileParser();
        try {
            zfp.addListener(new FileParserListener() {
                @Override
                public void filesCounted(int numFilesInZip) {
                    System.out.println("Total files in zip: "+numFilesInZip);
                }

                @Override
                public void fileProcessed(String fileName, Book book) {
                    System.out.println("--Loaded book from: "+fileName+"--\n"+book);
                }
            });
            List<Book> res = zfp.parseZipFile("F:\\Lsv\\JavaProjects\\LibsProcessing\\lib.rus.ec\\fb2-216642-221999.zip");
            System.out.println(""+res.size());
            for (Book book : res) {
                sess.save(book);                
            }
            List books = sess.createQuery("from Book").list();
            if (books.size() > 0) {
                System.out.println("Saved book:\n");
                System.out.println(""+books.get(0));
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

}
