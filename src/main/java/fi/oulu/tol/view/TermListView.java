package fi.oulu.tol.view;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import fi.oulu.tol.Settings;
import fi.oulu.tol.model.TermCategory;

public class TermListView extends JPanel {
    
    private JScrollPane scrollPane;
    private TermCategoryModel categories;
    private JList<TermCategory> list; 
    
    public TermListView(TermCategoryModel categories) {
        this.categories = categories;
        setPreferredSize(new Dimension(Settings.LIST_WIDTH, Settings.WINDOW_HEIGHT));
        list = new JList<>(categories);
        scrollPane = new JScrollPane(list);
        setBackground(Color.GREEN);
        add(scrollPane);
    }
}
