package fi.oulu.tol.view;

import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import fi.oulu.tol.model.TermCategory;
import fi.oulu.tol.model.TermProvider;
import fi.oulu.tol.model.TermProviderObserver;

public class TermCategoryListModel implements ListModel<TermCategory>, TermProviderObserver {

	private List<TermCategory> categories;
	private TermProvider provider;
	
	public TermCategoryListModel(TermProvider provider) {
		this.provider = provider;
		provider.addObserver(this);
		categories = provider.getCategories();
	}

	@Override
	public int getSize() {
		return categories.size();
	}

	@Override
	public TermCategory getElementAt(int index) {
		return categories.get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeEvent(Topic topic) {
		if (topic == Topic.CATEGORY_INDEX_CHANGED) {
			categories = provider.getCategories();
		}
	}

}
