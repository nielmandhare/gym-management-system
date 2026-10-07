/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.JComponent;

/**
 *
 * @author mgssr
 */
public class RoundedUtil {

    public static void roundedCorners(JComponent component, int radius) {
        component.putClientProperty(FlatClientProperties.STYLE, "arc:" + radius);
//        component.setOpaque(true);
    }
}
