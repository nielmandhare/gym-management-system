package util;

import connection.MySQL;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.Timer;

/** Adds lightweight, database-backed suggestions to a search field. */
public final class SearchSuggestions {
    private SearchSuggestions() { }

    public static void attach(JTextField field, JButton searchButton, String query) {
        JPopupMenu popup = new JPopupMenu();
        Timer debounce = new Timer(180, event -> refresh(field, searchButton, query, popup));
        debounce.setRepeats(false);
        field.setToolTipText("Type to see matching suggestions");
        field.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() { debounce.restart(); }
            @Override public void insertUpdate(DocumentEvent event) { changed(); }
            @Override public void removeUpdate(DocumentEvent event) { changed(); }
            @Override public void changedUpdate(DocumentEvent event) { changed(); }
        });
        field.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_DOWN && popup.getComponentCount() > 0) {
                    popup.setSelected(jMenu(popup));
                }
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) popup.setVisible(false);
            }
        });
    }

    private static JMenuItem jMenu(JPopupMenu popup) {
        return (JMenuItem) popup.getComponent(0);
    }

    private static void refresh(JTextField field, JButton searchButton, String query, JPopupMenu popup) {
        String text = field.getText().trim();
        popup.setVisible(false);
        popup.removeAll();
        if (text.isEmpty() || !field.isShowing()) return;
        try (ResultSet results = MySQL.executeSearch(query, text + "%")) {
            while (results.next() && popup.getComponentCount() < 8) {
                String value = results.getString(1);
                if (value == null || value.isBlank()) continue;
                JMenuItem item = new JMenuItem(value);
                item.addActionListener(event -> {
                    popup.setVisible(false);
                    field.setText(value);
                    searchButton.doClick();
                });
                popup.add(item);
            }
            if (popup.getComponentCount() > 0) popup.show(field, 0, field.getHeight());
        } catch (SQLException exception) {
            popup.setVisible(false);
        }
    }
}
