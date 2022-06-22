package fi.oulu.tol.model;

import org.json.JSONException;
import org.json.JSONObject;

public class TermCategory {
    String id;
    String nameEn;
    String nameFi;
    String nameSe;      // unused
    String termsURL;

    public static TermCategory from(JSONObject jsonObject) throws JSONException {
		TermCategory termCategory = new TermCategory();
		termCategory.id = jsonObject.getString("id");
		termCategory.nameEn = jsonObject.getString("nameEn");
        termCategory.nameFi = jsonObject.getString("nameFi");
        termCategory.nameSe = jsonObject.getString("nameSe");
        termCategory.termsURL = jsonObject.getString("termsURL");
		return termCategory;
	}

}
