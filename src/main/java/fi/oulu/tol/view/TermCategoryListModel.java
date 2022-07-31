package fi.oulu.tol.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import fi.oulu.tol.model.TermCategory;
import fi.oulu.tol.model.TermProvider;
import fi.oulu.tol.model.TermProviderObserver;

public class TermCategoryListModel implements ListModel<TermCategory>, TermProviderObserver {

	private List<TermCategory> categories;
	private TermProvider provider;
	private ArrayList<ListDataListener> listeners = new ArrayList<>();

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
		listeners.add(l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}

	@Override
	public void changeEvent(Topic topic) {
		if (topic == Topic.CATEGORY_INDEX_CHANGED) {
			categories = provider.getCategories();
			for (ListDataListener listener : listeners) {
				listener.intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, categories.size() - 1));
			}
		} else if (topic == Topic.CATEGORY_CHANGED) {
			categories = provider.getCategories();
			for (ListDataListener listener : listeners) {
				listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, categories.size() - 1));
			}
		}
	}

}
