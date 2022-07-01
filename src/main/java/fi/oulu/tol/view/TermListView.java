package fi.oulu.tol.view;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.sql.SQLException;
import java.awt.BorderLayout;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.json.JSONException;

import fi.oulu.tol.Settings;
import fi.oulu.tol.model.Term;
import fi.oulu.tol.model.TermProvider;

public class TermListView extends JPanel implements ListSelectionListener {

	private JScrollPane scrollPane;
	private TermListModel terms;
	private JList<Term> list;
	private TermProvider provider;

	public TermListView(TermProvider provider) throws JSONException, SQLException, IOException {
		super(new BorderLayout());
		this.provider = provider;
		terms = new TermListModel(provider);
		setPreferredSize(new Dimension(Settings.LIST_WIDTH, Settings.WINDOW_HEIGHT));
		list = new JList<>();
		list.setCellRenderer(new TermRowRenderer());
		list.setModel(terms);
		list.addListSelectionListener(this);
		scrollPane = new JScrollPane(list);
		add(scrollPane, BorderLayout.CENTER);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		
	}
}
