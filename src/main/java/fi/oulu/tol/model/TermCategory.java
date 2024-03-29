package fi.oulu.tol.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import fi.oulu.tol.Settings;

public class TermCategory implements Comparable<TermCategory> {
	String id;
	String nameEn;
	String nameFi;
	String nameSe; // unused
	String aboutURL;
	String termsURL;
	LocalDateTime updated;

	public static TermCategory from(JSONObject jsonObject) throws JSONException {
		TermCategory termCategory = new TermCategory();
		termCategory.id = jsonObject.getString("id");
		termCategory.nameEn = jsonObject.getString("nameEn");
		termCategory.nameFi = jsonObject.getString("nameFi");
		termCategory.nameSe = jsonObject.getString("nameSe");
		termCategory.aboutURL = jsonObject.getString("aboutURL");
		termCategory.termsURL = jsonObject.getString("termsURL");
		Date weekAgo = new Date(System.currentTimeMillis() - 3600 * 24 * 7 * 1000);
		termCategory.updated = weekAgo.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
		return termCategory;
	}

	@Override
	public int compareTo(TermCategory o) {
		return id.compareTo(o.id);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TermCategory) {
			return ((TermCategory) o).id.equals(id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		switch (Settings.language) {
		case FINNISH:
			return nameFi;
		case ENGLISH:
			return nameEn;
		}
		return nameFi;
	}
}
