import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import ru.lsv.lib.ui.MainForm;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by IntelliJ IDEA.
 * User: Сергей
 * Date: 23.09.2010
 * Time: 20:50:28
 */
public class MainClass {

    public static void main(String[] argc) {
        configureUI();
        MainForm mainForm = new MainForm();
        mainForm.buildMainFrame();
    }

    private static void configureUI() {
        UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
        Options.setDefaultIconSize(new Dimension(18, 18));

        String lafName =
            LookUtils.IS_OS_WINDOWS_XP
                ? Options.getCrossPlatformLookAndFeelClassName()
                : Options.getSystemLookAndFeelClassName();

        try {
            UIManager.setLookAndFeel(lafName);
        } catch (Exception e) {
            System.err.println("Can't set look & feel:" + e);
        }
    }


}
