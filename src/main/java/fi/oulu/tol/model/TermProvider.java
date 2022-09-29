package fi.oulu.tol.model;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
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
			logger.debug("Putting the categories from db to model");
			updateMap(categories);
		}
	}

	public void close() {
		logger.debug("Clearing the observers");
		observers.clear();
		logger.debug("Closing the database.");
		database.close();
		logger.info("Closed the TermProvider");
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
			logger.debug("No selected category, returning empty list of terms");
			return new ArrayList<>();
		}
		List<Term> terms = categoriesAndTerms.get(selectedCategory);
		if (terms != null) {
			terms = database.readTerms(selectedCategory.id, sortOrder);
			if (terms.isEmpty()) {
				logger.debug("No terms in db for category, fetching " + selectedCategory.id);
				terms = fetchTerms(selectedCategory);
			}
			if (!terms.isEmpty()) {
				logger.debug("Db or server gave non-empty list of terms, taking into use");
				terms = terms.stream().sorted(comparator()).toList();
				categoriesAndTerms.put(selectedCategory, terms);
			}
		} else {
			logger.debug("No terms for this (nonexistent?) category, returning empty list of terms");
			terms = new ArrayList<>();
		}
		if (searchFilter.length() > 0) {
			logger.debug("Search filter used, filtering terms");
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
		logger.info("Fetching terms from remote server");
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

	public List<Term> getTermsFor(TermCategory category) {
		return categoriesAndTerms.get(category);
	}

	public List<Term> getAllTerms() throws SQLException {
		return database.readAllTerms();
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
			logger.debug("Sorting all the terms since sort order changed");
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
			logger.debug("Setting the search filter to " + filter);
			searchFilter = filter;
			notifyObservers(Topic.CATEGORY_TERMS_CHANGED);
		}
	}

	public String getSearchFilter() {
		return searchFilter;
	}

	public void addObserver(TermProviderObserver observer) {
		observers.add(observer);
	}

	public void removeObserver(TermProviderObserver observer) {
		observers.remove(observer);
	}

	private void notifyObservers(TermProviderObserver.Topic topic) {
		for (TermProviderObserver observer: observers) {
			observer.changeEvent(topic);
		}
	}
}
