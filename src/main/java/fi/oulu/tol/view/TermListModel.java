package fi.oulu.tol.view;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.json.JSONException;

import fi.oulu.tol.model.Term;
import fi.oulu.tol.model.TermProvider;
import fi.oulu.tol.model.TermProviderObserver;

public class TermListModel implements ListModel<Term>, TermProviderObserver {

	private TermProvider provider;
	private List<Term> terms;
	private ArrayList<ListDataListener> listeners = new ArrayList<>();

	public TermListModel(TermProvider provider) throws JSONException, SQLException, IOException {
		this.provider = provider;
		provider.addObserver(this);
		this.terms = provider.getSelectedCategoryTerms();
	}

	@Override
	public int getSize() {
		if (null != terms) {
			return terms.size();
		}
		return 0;
	}

	@Override
	public Term getElementAt(int index) {
		if (null != terms && index >= 0 && index < terms.size()) {
			return terms.get(index);
		}
		return null;
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
		if (topic == Topic.SELECTED_CATEGORY_CHANGED || topic == Topic.CATEGORY_TERMS_CHANGED) {
			try {
				this.terms = provider.getSelectedCategoryTerms();
				for (ListDataListener listener : listeners) {
					listener.intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, terms.size()));
				}
			} catch (JSONException | SQLException | IOException e) {
				e.printStackTrace();
			}
		} else if (topic == Topic.CATEGORY_CHANGED) {
			try {
				this.terms = provider.getSelectedCategoryTerms();
				for (ListDataListener listener : listeners) {
					listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, terms.size()));
				}
			} catch (JSONException | SQLException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
