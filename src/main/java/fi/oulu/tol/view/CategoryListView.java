package fi.oulu.tol.view;

import java.awt.Dimension;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import fi.oulu.tol.Settings;
import fi.oulu.tol.model.TermCategory;
import fi.oulu.tol.model.TermProvider;

public class CategoryListView extends JPanel implements ListSelectionListener {

	private JScrollPane scrollPane;
	private TermCategoryModel categories;
	private JList<TermCategory> list;
	private TermProvider provider;

	public CategoryListView(TermProvider provider) {
		this.provider = provider;
		setPreferredSize(new Dimension(Settings.LIST_WIDTH, Settings.WINDOW_HEIGHT));
		categories = new TermCategoryModel(provider.getCategories());
		list = new JList<>(categories);
		scrollPane = new JScrollPane(list);
		list.addListSelectionListener(this);
		if (categories.getSize() > 0) {
			list.setSelectedIndex(0);
		}
		add(scrollPane);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		provider.setSelectedCategory(list.getSelectedValue());

	}
}
