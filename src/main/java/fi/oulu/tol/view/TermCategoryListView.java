package fi.oulu.tol.view;

import java.awt.Dimension;
import java.awt.BorderLayout;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import fi.oulu.tol.Settings;
import fi.oulu.tol.model.TermCategory;
import fi.oulu.tol.model.TermProvider;

public class TermCategoryListView extends JPanel implements ListSelectionListener {

	private JScrollPane scrollPane;
	private transient TermCategoryListModel categories;
	private JList<TermCategory> list;
	private transient TermProvider provider;

	public TermCategoryListView(TermProvider provider) {
		super(new BorderLayout());
		this.provider = provider;
		setMinimumSize(new Dimension(Settings.LIST_WIDTH, Settings.WINDOW_HEIGHT));
		setPreferredSize(new Dimension(Settings.LIST_WIDTH, Settings.WINDOW_HEIGHT));
		categories = new TermCategoryListModel(provider);
		list = new JList<>(categories);
		list.setCellRenderer(new CategoryRowRenderer());
		scrollPane = new JScrollPane(list);
		list.addListSelectionListener(this);
		if (categories.getSize() > 0) {
			list.setSelectedIndex(0);
		}
		add(scrollPane, BorderLayout.CENTER);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		provider.setSelectedCategory(list.getSelectedValue());
	}

}
