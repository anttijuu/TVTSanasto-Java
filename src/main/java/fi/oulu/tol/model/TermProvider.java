package fi.oulu.tol.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.oulu.tol.networking.Downloader;

public class TermProvider {

	private Map<TermCategory, List<Term>> terms = new HashMap<>();
	private LocalDatabase database = new LocalDatabase();
	private Downloader network = new Downloader();
	private TermCategory selectedCategory;
	private Set<TermProviderObserver> observers = new HashSet<>();

	public TermProvider() throws SQLException, IOException {
		database.open("test.sqlite");
		List<TermCategory> categories = database.readCategories();
		if (categories.isEmpty()) {
			fetchIndex();
		} else {
			updateMap(categories);
		}
	}

	public void fetchIndex() throws SQLException, IOException {
		List<TermCategory> categories = network.getIndex();
		database.saveCategories(categories);
		updateMap(categories);
	}

	public List<TermCategory> getCategories() {
		return terms.keySet().stream().toList();
	}

	public List<Term> getSelectedCategoryTerms() {
		return terms.get(selectedCategory);
	}

	public List<Term> getTerms(TermCategory forCategory) {
		return terms.get(forCategory);
	}

	private void updateMap(List<TermCategory> fromCategories) {
		for (TermCategory category : fromCategories) {
			if (!terms.containsKey(category)) {
				terms.put(category, new ArrayList<>());
			}
		}
	}

	public void setSelectedCategory(TermCategory source) {
		selectedCategory = source;
	}

	void addObserver(TermProviderObserver observer) {
		observers.add(observer);
	}

	void removeObserver(TermProviderObserver observer) {
		observers.remove(observer);
	}
}
