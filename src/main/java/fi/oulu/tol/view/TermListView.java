package fi.oulu.tol.view;

import java.awt.Dimension;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.json.JSONException;

import fi.oulu.tol.Settings;
import fi.oulu.tol.model.Term;
import fi.oulu.tol.model.TermProvider;

public class TermListView extends JPanel implements ListSelectionListener, ListDataListener {

	private JScrollPane scrollPane;
	private transient TermListModel terms;
	private JLabel countLabel;
	private JList<Term> list;
	private transient TermProvider provider;

	public TermListView(TermProvider provider) throws JSONException, SQLException, IOException, URISyntaxException {
		super(new BorderLayout());
		this.provider = provider;
		terms = new TermListModel(provider);
		setMinimumSize(new Dimension(Settings.LIST_WIDTH, Settings.WINDOW_HEIGHT));
		setPreferredSize(new Dimension(Settings.LIST_WIDTH, Settings.WINDOW_HEIGHT));
		countLabel = new JLabel(terms.getSize() + " termiä");
		add(countLabel, BorderLayout.NORTH);
		list = new JList<>();
		list.setCellRenderer(new TermRowRenderer());
		list.setModel(terms);
		list.addListSelectionListener(this);
		terms.addListDataListener(this);
		scrollPane = new JScrollPane(list);
		add(scrollPane, BorderLayout.CENTER);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		provider.setSelectedTerm(list.getSelectedValue());
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		countLabel.setText(terms.getSize() + " termiä");
		list.setSelectedIndex(0);
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		countLabel.setText(terms.getSize() + " termiä");
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
	}
}
