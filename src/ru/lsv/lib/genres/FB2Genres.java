package ru.lsv.lib.genres;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.PropertyResourceBundle;

/**
 * User: Lsv
 * Date: 13.10.2010
 * Time: 13:35:04
 * To change this template use File | Settings | File Templates.
 */
public class FB2Genres {

    public static Properties genres = new Properties();

    static {
        // Придется тут все читать из Reader'а...
        try {
            genres.load(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream("ru/lsv/lib/resources/fb2Genres.properties"), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();  
        }
    }

}
