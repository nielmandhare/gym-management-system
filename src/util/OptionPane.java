/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.awt.Component;
import java.awt.Font;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 *
 * @author mgssr
 */
public class OptionPane {

    public static void showMessage(Component parent, String title, String message, String type) {

        UIManager.put("OptionPane.messageFont", new Font("Poppins", Font.PLAIN, 14));
        UIManager.put("OptionPane.buttonFont", new Font("Poppins", Font.PLAIN, 14));

        if (null == type) {
            JOptionPane.showMessageDialog(parent, message, title, JOptionPane.DEFAULT_OPTION);
        } else switch (type) {
            case "ERROR" -> JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
            case "WARNING" -> JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
            case "INFORMATION" -> JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
            default -> JOptionPane.showMessageDialog(parent, message, title, JOptionPane.DEFAULT_OPTION);
        }
    }
}
