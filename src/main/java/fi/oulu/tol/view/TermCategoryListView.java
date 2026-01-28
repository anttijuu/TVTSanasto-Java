package fi.oulu.tol.view;

import java.awt.Dimension;
import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListSelectionEvent;

import java.text.MessageFormat;
import java.text.ChoiceFormat;
import java.text.Format;
import java.text.NumberFormat;

import java.util.ResourceBundle;

import fi.oulu.tol.Settings;
import fi.oulu.tol.model.TermCategory;
import fi.oulu.tol.model.TermProvider;
import fi.oulu.tol.model.TermProviderObserver;

public class TermCategoryListView extends JPanel implements javax.swing.event.ListSelectionListener, javax.swing.event.ListDataListener, fi.oulu.tol.model.TermProviderObserver {

	private JScrollPane scrollPane;
	private transient TermCategoryListModel categories;
	private JLabel countLabel;
	private JList<TermCategory> list;
	private transient TermProvider provider;

	public TermCategoryListView(TermProvider provider) {
		super(new BorderLayout());
		this.provider = provider;
		this.provider.addObserver(this);
		setMinimumSize(new Dimension(Settings.LIST_WIDTH, Settings.WINDOW_HEIGHT));
		setPreferredSize(new Dimension(Settings.LIST_WIDTH, Settings.WINDOW_HEIGHT));
		categories = new TermCategoryListModel(provider);		
		countLabel = new JLabel(categories.getSize() + " kategoriaa");
		add(countLabel, BorderLayout.NORTH);
		list = new JList<>(categories);
		list.setCellRenderer(new CategoryRowRenderer());
		scrollPane = new JScrollPane(list);
		list.addListSelectionListener(this);
		if (categories.getSize() > 0) {
			list.setSelectedIndex(0);
		}
		add(scrollPane, BorderLayout.CENTER);
		updateCountLabel();
	}

	private void updateCountLabel() {
		ResourceBundle messages = ResourceBundle.getBundle("TermCategoryListViewBundle", Settings.currentLocale());
		MessageFormat messageForm = new MessageFormat("pattern");
		messageForm.setLocale(Settings.currentLocale());
		double[] termCountLimits = {0,1,2};
		String [] termStrings = {
			messages.getString("no_categories"),
			messages.getString("one_category"),
			messages.getString("many_categories")
		};
		ChoiceFormat choiceForm = new ChoiceFormat(termCountLimits, termStrings);
		String pattern = messages.getString("pattern");
		messageForm.applyPattern(pattern);
		Format[] formats = {choiceForm, null, NumberFormat.getInstance()};
		messageForm.setFormats(formats);
		Object[] messageArguments = {categories.getSize()};
		final String result = messageForm.format(messageArguments);
		countLabel.setText(result);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		provider.setSelectedCategory(list.getSelectedValue());
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		updateCountLabel();
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
