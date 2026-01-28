package fi.oulu.tol.model;

public interface TermProviderObserver {
	public enum Topic {
		CATEGORY_INDEX_CHANGED,
		CATEGORY_CHANGED,
		CATEGORY_TERMS_CHANGED,
		SELECTED_CATEGORY_CHANGED,
		SELECTED_TERM_CHANGED,
		LANGUAGE_CHANGED
	}
	public void changeEvent(Topic topic);
}
