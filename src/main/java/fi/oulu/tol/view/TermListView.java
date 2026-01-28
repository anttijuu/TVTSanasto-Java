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
import fi.oulu.tol.model.TermProviderObserver;

public class TermListView extends JPanel implements ListSelectionListener, ListDataListener, TermProviderObserver {

	private JScrollPane scrollPane;
	private transient TermListModel terms;
	private JLabel countLabel;
	private JList<Term> list;
	private transient TermProvider provider;

	public TermListView(TermProvider provider) throws JSONException, SQLException, IOException, URISyntaxException {
		super(new BorderLayout());
		this.provider = provider;
		this.provider.addObserver(this);
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
		updateCountLabel();
	}

	private void updateCountLabel() {
		java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("TermListViewBundle", Settings.currentLocale());
		java.text.MessageFormat messageForm = new java.text.MessageFormat("");
		messageForm.setLocale(Settings.currentLocale());
		double[] termCountLimits = {0,1,2};
		String [] termStrings = {
			messages.getString("no_terms"),
			messages.getString("one_term"),
			messages.getString("many_terms")
		};
		java.text.ChoiceFormat choiceForm = new java.text.ChoiceFormat(termCountLimits, termStrings);
		String pattern = messages.getString("pattern");
		messageForm.applyPattern(pattern);
		java.text.Format[] formats = {choiceForm, null, java.text.NumberFormat.getInstance()};
		messageForm.setFormats(formats);
		Object[] messageArguments = {terms.getSize()};
		final String result = messageForm.format(messageArguments);
		countLabel.setText(result);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		provider.setSelectedTerm(list.getSelectedValue());
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		updateCountLabel();
		list.setSelectedIndex(0);
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		updateCountLabel();
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
	}

	@Override
	public void changeEvent(TermProviderObserver.Topic topic) {
		if (topic == Topic.LANGUAGE_CHANGED) {
			updateCountLabel();
		}
	}
}
