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

	private Map<TermCategory, List<Term>> categoriesAndTerms = new HashMap<>();
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
		return categoriesAndTerms.keySet().stream().toList();
	}

	public List<Term> getSelectedCategoryTerms() throws SQLException, JSONException, IOException {
		List<Term> terms = categoriesAndTerms.get(selectedCategory);
		if (terms.isEmpty()) {
			terms = database.readTerms(selectedCategory.id);
			if (terms.isEmpty()) {
				terms = fetchTerms(selectedCategory);
			}
			categoriesAndTerms.put(selectedCategory, terms);
		}
		return terms;
	}

	private List<Term> fetchTerms(TermCategory category) throws JSONException, IOException, SQLException {
		logger.info("Fetching terms from remote.");
		List<Term> fetchedTerms = network.getTerms(category.termsURL);
		categoriesAndTerms.put(category, fetchedTerms);
		logger.info("Saving fetched terms to the local db.");
		database.saveTerms(fetchedTerms, category.id);
		return fetchedTerms;
	}

	public List<Term> getTerms(TermCategory forCategory) {
		return categoriesAndTerms.get(forCategory);
	}

	private void updateMap(List<TermCategory> fromCategories) throws JSONException, IOException, SQLException {
		for (TermCategory category : fromCategories) {
			if (!categoriesAndTerms.containsKey(category)) {
				logger.info("Adding a new category to map.");
				categoriesAndTerms.put(category, new ArrayList<>());
			} else {
				// Category already in map.
			}
		}
	}

	public void setSelectedCategory(TermCategory category) {
		if (selectedCategory != category) {
			selectedCategory = category;
			notifyObservers(Topic.SELECTED_CATEGORY_CHANGED);	
		}
	}

	public void setSelectedTerm(Term term) {
		if (selectedTerm != term) {
			selectedTerm = term;
			notifyObservers(Topic.SELECTED_TERM_CHANGED);	
		}
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
