package util;

import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 *
 * @author mgssr
 */
public class AppIconUtil {

    private static Image favicon;

    static {
        try {
            URL iconPath = AppIconUtil.class.getResource("/img/favicon.png");
            if (iconPath == null) {
                throw new NullPointerException("Favicon not found!");
            }
            ImageIcon icon = new ImageIcon(iconPath);
            AppIconUtil.favicon = icon.getImage();
        } catch (NullPointerException e) {
            OptionPane.showMessage(null, "Error Message", "Missing Favicon!    ", "ERROR");
        }
    }

    public static void applyIcon(JFrame frame) {
        if (frame != null && favicon != null) {
            frame.setIconImage(favicon);
        }
    }
}
