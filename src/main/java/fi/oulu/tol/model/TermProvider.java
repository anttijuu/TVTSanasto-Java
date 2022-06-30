package fi.oulu.tol.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.JSONException;

import fi.oulu.tol.model.TermProviderObserver.Topic;
import fi.oulu.tol.networking.Downloader;

public class TermProvider {

	private Map<TermCategory, List<Term>> terms = new HashMap<>();
	private LocalDatabase database = new LocalDatabase();
	private Downloader network = new Downloader();
	private TermCategory selectedCategory;
	private Term selectedTerm;
	private Set<TermProviderObserver> observers = new HashSet<>();

	private static final Logger logger = LogManager.getLogger(TermProvider.class);
	
	public TermProvider() throws SQLException, IOException {
		logger.info("Initializing TermProvider.");
		database.open("test.sqlite");
		logger.info("Reading categories from local database.");
		List<TermCategory> categories = database.readCategories();
		if (categories.isEmpty()) {
			logger.info("No categories in local db, fetching remote index.");
			fetchIndex();
		} else {
			updateMap(categories);
		}
	}

	public void fetchIndex() throws SQLException, IOException {
		logger.info("Fetching remote category index.");
		List<TermCategory> categories = network.getIndex();
		logger.info("Saving categories to local db.");
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

	private void updateMap(List<TermCategory> fromCategories) throws JSONException, IOException, SQLException {
		for (TermCategory category : fromCategories) {
			if (!terms.containsKey(category)) {
				logger.info("Adding a new category to hashmap.");
				terms.put(category, new ArrayList<>());
				if (terms.get(category).isEmpty()) {
					logger.info("Known and empty category, fetching terms from remote.");
					List<Term> fetchedTerms = network.getTerms(category.termsURL);
					terms.put(category, fetchedTerms);
					logger.info("Saving fetched terms to the local db.");
					database.saveTerms(fetchedTerms, category.id);
				}
			}
		}
	}

	public void setSelectedCategory(TermCategory source) {
		selectedCategory = source;
		notifyObservers(Topic.SELECTED_CATEGORY_CHANGED);
	}

	public void setSelectedTerm(Term term) {
		selectedTerm = term;
		notifyObservers(Topic.SELECTED_TERM_CHANGED);
	}

	public void addObserver(TermProviderObserver observer) {
		observers.add(observer);
	}

	public void removeObserver(TermProviderObserver observer) {
		observers.remove(observer);
	}

	private void notifyObservers(TermProviderObserver.Topic topic) {
		logger.debug("Notifying observers of topic " + topic);
		for (TermProviderObserver observer: observers) {
			observer.changeEvent(topic);
		}
	}
}
