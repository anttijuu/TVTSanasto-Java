package fi.oulu.tol.view;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import fi.oulu.tol.Settings;
import fi.oulu.tol.model.Term;
import fi.oulu.tol.model.TermCategory;
import fi.oulu.tol.model.TermProvider;

public class TermListView extends JPanel {

	private JScrollPane scrollPane;
	private TermListModel terms;
	private JList<Term> list;
	private TermProvider provider;

	public TermListView(TermProvider provider) {
		this.provider = provider;
		terms = new TermListModel(provider.getSelectedCategoryTerms());
		setPreferredSize(new Dimension(Settings.LIST_WIDTH, Settings.WINDOW_HEIGHT));
		list = new JList<>();
		scrollPane = new JScrollPane(list);
		setBackground(Color.GREEN);
		add(scrollPane);
	}
}
