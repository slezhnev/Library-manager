package ru.lsv.lib.parsers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import ru.lsv.lib.common.Author;
import ru.lsv.lib.common.Book;
import ru.lsv.lib.common.HibernateUtil;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 * Парсер fb2-книг из потока
 * User: lsv
 * Date: 20.10.2010
 * Time: 12:35:13
 */
public class BookParser extends DefaultHandler {

    private String tempVal;
    private Book tempBook = null;
    private Book retBook = null;
    private Author tempAuthor = null;

    public Book parseFB2Stream(InputStream inStream, int id, String zipFileName) throws SAXException, ParserConfigurationException, IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser parser = spf.newSAXParser();
        parser.parse(inStream, this);
        if (retBook != null) {
            retBook.setId(id);
            retBook.setZipFileName(zipFileName);
        }
        return retBook;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("title-info")) {
            tempBook = new Book();
        } else if (qName.equalsIgnoreCase("author")) {
            if (tempBook != null)
                tempAuthor = new Author();
        } else if (qName.equalsIgnoreCase("sequence")) {
            if (tempBook != null) {
                tempBook.setSerieName(attributes.getValue("name"));
                try {
                    tempBook.setNumInSerie(Integer.parseInt(attributes.getValue("number")));
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }


    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {


        if (qName.equalsIgnoreCase("title-info")) {
            retBook = tempBook;
            tempBook = null;
        } else if (qName.equalsIgnoreCase("genre")) {
            if (tempBook != null) tempBook.setGenre(tempVal);
        } else if (qName.equalsIgnoreCase("first-name")) {
            if (tempAuthor != null) tempAuthor.setFirstName(tempVal);
        } else if (qName.equalsIgnoreCase("middle-name")) {
            if (tempAuthor != null) tempAuthor.setMiddleName(tempVal);
        } else if (qName.equalsIgnoreCase("last-name")) {
            if (tempAuthor != null) tempAuthor.setLastName(tempVal);
        } else if (qName.equalsIgnoreCase("author")) {
            if (tempBook != null) {
                // Дополнительно уже прямо тут будем обрабатывать сохранение в DB!
                tempAuthor = HibernateUtil.getAuthorFromDB(tempAuthor);
                // TODO Доделать сохранение книги в автора!
                tempBook.getAuthors().add(tempAuthor);
                tempAuthor = null;
            }
        } else if (qName.equalsIgnoreCase("book-title")) {
            if (tempBook != null) tempBook.setTitle(tempVal);
        } else if (qName.equalsIgnoreCase("lang")) {
            if (tempBook != null) tempBook.setLanguage(tempVal);
        } else if (qName.equalsIgnoreCase("src-lang")) {
            if (tempBook != null) tempBook.setSourceLanguage(tempVal);
        }

    }

}
