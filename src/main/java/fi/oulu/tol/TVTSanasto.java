package fi.oulu.tol;

import java.awt.Dimension;
import javax.swing.JFrame;
import java.awt.FlowLayout;

import java.io.IOException;
import java.sql.SQLException;

import fi.oulu.tol.model.Term;
import fi.oulu.tol.model.TermProvider;
import fi.oulu.tol.view.CategoryListView;
import fi.oulu.tol.view.TermCategoryModel;
import fi.oulu.tol.view.TermDetailView;
import fi.oulu.tol.view.TermListView;

public class TVTSanasto {

    private JFrame frame;
    private TermProvider provider;

    public static void main(String[] args) {
        try {
            new TVTSanasto().run();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run() throws SQLException, IOException {
        provider = new TermProvider();
        frame = new JFrame("TVT Sanasto");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT));
        frame.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        
        frame.add(new CategoryListView(provider));
        frame.add(new TermListView(new TermCategoryModel(provider.getCategories())));
        frame.add(new TermDetailView(new Term()));
        frame.pack();
        frame.setVisible(true);
    }
}