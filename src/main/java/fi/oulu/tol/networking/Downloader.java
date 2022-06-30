package fi.oulu.tol.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import fi.oulu.tol.model.Term;
import fi.oulu.tol.model.TermCategory;

public class Downloader {
	private static final String INDEX_URL = "https://juustila.com/apps/tira-sanasto/test-index.json";
	private static final String CONTENT_TYPE = "application/json";

	private static final int CONNECT_TIMEOUT = 10 * 1000;
	private static final int REQUEST_TIMEOUT = 30 * 1000;

	private static final Logger logger = LogManager.getLogger(Downloader.class);

	public synchronized List<TermCategory> getIndex() throws IOException {
		logger.info("Starting to fetch term indices from " + INDEX_URL);
		HttpsURLConnection connection = setupConnection(INDEX_URL);
		List<TermCategory> results = new ArrayList<>();
		int responseCode = connection.getResponseCode();
		logger.info("HTTPS response code: " + responseCode);
		if (responseCode == 204) {
			return results;
		} else if (responseCode >= 200 && responseCode < 300) {
			String input;
			BufferedReader in = new BufferedReader(
					new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
			StringBuilder builder = new StringBuilder();
			while ((input = in.readLine()) != null) {
				builder.append(input);
			}
			logger.debug("Starting to parse received JSON");
			JSONArray jsonArray = new JSONArray(builder.toString());
			if (jsonArray.length() > 0) {
				logger.info("Received " + jsonArray.length() + " items");
				for (int index = 0; index < jsonArray.length(); index++) {
					JSONObject object = jsonArray.getJSONObject(index);
					TermCategory termCategory = TermCategory.from(object);
					results.add(termCategory);
				}
			}
			in.close();
		} else {
			logger.error("Error in response: " + responseCode);
			throw new IOException("Could not fetch server index");
		}
		return results;
	}

	public synchronized List<Term> getTerms(String forCategoryURL) throws IOException, JSONException {
		List<Term> terms = new ArrayList<>();
		logger.info("Starting to fetch terms from " + forCategoryURL);
		HttpsURLConnection connection = setupConnection(forCategoryURL);
		int responseCode = connection.getResponseCode();
		logger.info("HTTPS response code: " + responseCode);
		if (responseCode == 204) {
			return terms;
		} else if (responseCode >= 200 && responseCode < 300) {
			String input;
			BufferedReader in = new BufferedReader(
					new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
			StringBuilder builder = new StringBuilder();
			while ((input = in.readLine()) != null) {
				builder.append(input);
			}
			logger.debug("Starting to parse received JSON");
			JSONArray jsonArray = new JSONArray(builder.toString());
			if (jsonArray.length() > 0) {
				logger.debug("Received " + jsonArray.length() + " terms");
				for (int index = 0; index < jsonArray.length(); index++) {
					JSONObject object = jsonArray.getJSONObject(index);
					Term term = Term.from(object);
					terms.add(term);
				}
			}
			in.close();
		} else {
			logger.error("Failed to fetch terms since: " + responseCode);
			throw new IOException("Could not fetch terms");
		}
		return terms;
	}

	private HttpsURLConnection setupConnection(String forURL) throws IOException {
		URL url = new URL(forURL);
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		connection.setUseCaches(false);
		connection.setDefaultUseCaches(false);
		connection.setRequestProperty("Cache-Control", "no-cache");

		connection.setRequestMethod("GET");
		connection.setRequestProperty("Content-Type", CONTENT_TYPE);
		// All requests use these common timeouts.
		connection.setConnectTimeout(CONNECT_TIMEOUT);
		connection.setReadTimeout(REQUEST_TIMEOUT);
		return connection;
	}
}
