package fi.oulu.tol.model;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.JSONException;

import fi.oulu.tol.Settings;
import fi.oulu.tol.model.TermProviderObserver.Topic;
import fi.oulu.tol.networking.Downloader;

public class TermProvider {

	private Map<TermCategory, List<Term>> categoriesAndTerms = new HashMap<>();
	private LocalDatabase database = new LocalDatabase();
	private Downloader network = new Downloader();
	private TermCategory selectedCategory;
	private Term selectedTerm;
	private Set<TermProviderObserver> observers = new HashSet<>();
	private Language sortOrder = Settings.language;
	private String searchFilter = "";

	private static final int FETCH_TIME_GAP_HOURS = 6; // hrs between network fetches.

	private static final Logger logger = LogManager.getLogger(TermProvider.class);
	
	public TermProvider() throws SQLException, IOException {
		logger.info("Initializing TermProvider.");
		database.open("tvtsanasto.sqlite");
		logger.info("Reading categories from local database.");
		List<TermCategory> categories = database.readCategories();
		if (categories.isEmpty()) {
			logger.info("No categories in local db, fetching remote index.");
			fetchIndex();
		} else {
			updateMap(categories);
		}
	}

	public void close() {
		logger.debug("Clearing the observers");
		observers.clear();
		logger.debug("Closing the database.");
		database.close();
	}

	public int fetchIndex() throws SQLException, IOException {
		LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
		// If no categories locally, do always fetch. Otherwise check if fetching too frequently.
		if (!categoriesAndTerms.isEmpty()) {
			Duration diff = Duration.between(Settings.lastIndexFetchDateTime, now);
			if (diff.toHours() < FETCH_TIME_GAP_HOURS) {
				logger.info("Not fetching until after timeout from previous fetch.");
				return -1;
			}
		}
		logger.info("Fetching remote category index.");
		int currentCount = categoriesAndTerms.size();
		List<TermCategory> categories = network.getIndex();
		logger.info("Saving categories to local db.");
		database.saveCategories(categories);
		updateMap(categories);
		Settings.lastIndexFetchDateTime = LocalDateTime.now(ZoneOffset.UTC);
		Settings.saveSettings();
		return categories.size() - currentCount;
	}

	public List<TermCategory> getCategories() {
		return categoriesAndTerms.keySet().stream().toList();
	}

	public List<Term> getSelectedCategoryTerms() throws SQLException, JSONException, IOException {
		if (selectedCategory == null) {
			return new ArrayList<>();
		}
		List<Term> terms = categoriesAndTerms.get(selectedCategory);
		if (terms != null) {
			terms = database.readTerms(selectedCategory.id);
			if (terms.isEmpty()) {
				terms = fetchTerms(selectedCategory);
			}
			if (!terms.isEmpty()) {
				categoriesAndTerms.put(selectedCategory, terms.stream().sorted(comparator()).toList());
			}
		} else {
			terms = new ArrayList<>();
		}
		if (searchFilter.length() > 0) {
			return terms.stream().filter(term -> term.description().contains(searchFilter)).toList();
		}
		return terms;
	}

	private Comparator<? super Term> comparator() {
		if (sortOrder == Language.FINNISH) {
			return (p1, p2) -> { return p1.finnish.toLowerCase().compareTo(p2.finnish.toLowerCase()); };
		}
		return (p1, p2) -> { return p1.english.toLowerCase().compareTo(p2.english.toLowerCase()); };
	}

	public List<Term> fetchTerms(TermCategory category) throws JSONException, IOException, SQLException {
		if (null == category) {
			return new ArrayList<>();
		}
		LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
		Duration diff = Duration.between(category.updated, now);
		if (diff.toHours() < FETCH_TIME_GAP_HOURS) {
			logger.info("Not fetching the category until after timeout from previous fetch.");
			return new ArrayList<>();
		}
		logger.info("Fetching terms from remote.");
		List<Term> fetchedTerms = network.getTerms(category.termsURL).stream().sorted(comparator()).toList();
		category.updated = now;
		categoriesAndTerms.put(category, fetchedTerms);
		logger.info("Saving fetched terms to the local db.");
		database.saveTerms(fetchedTerms, category);
		return fetchedTerms;
	}

	public Term getSelectedTerm() {
		return selectedTerm;
	}

	private void updateMap(List<TermCategory> fromCategories) {
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

	public TermCategory getSelectedCategory() {
		return selectedCategory;
	}

	public void setSelectedTerm(Term term) {
		if (selectedTerm != term) {
			selectedTerm = term;
			notifyObservers(Topic.SELECTED_TERM_CHANGED);	
		}
	}

	public Language getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Language order) {
		if (order != sortOrder) {
			sortOrder = order;
			for (Map.Entry<TermCategory, List<Term>> entry : categoriesAndTerms.entrySet()) {
				TermCategory category = entry.getKey();
				List<Term> terms = entry.getValue();
				categoriesAndTerms.put(category, terms.stream().sorted(comparator()).toList());
		  	}
			notifyObservers(Topic.CATEGORY_CHANGED);
		}
	}

	public void setSearchFilter(String filter) {
		if (!searchFilter.equals(filter)) {
			searchFilter = filter;
			notifyObservers(Topic.SELECTED_CATEGORY_CHANGED);
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
