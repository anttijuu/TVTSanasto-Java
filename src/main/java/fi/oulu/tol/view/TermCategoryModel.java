package fi.oulu.tol.view;

import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import fi.oulu.tol.model.TermCategory;

public class TermCategoryModel implements ListModel<TermCategory> {

	private List<TermCategory> categories;

	public TermCategoryModel(List<TermCategory> categories) {
		this.categories = categories;
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

}
