package fi.oulu.tol.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import fi.oulu.tol.model.TermCategory;

public class Downloader {
    private static final String INDEX_URL = "https://juustila.com/apps/tira-sanasto/test-index.json";
    private static final String CONTENT_TYPE = "application/json";

	private static final int CONNECT_TIMEOUT = 10 * 1000;
	private static final int REQUEST_TIMEOUT = 30 * 1000;

	public synchronized List<TermCategory> getIndex() throws IOException {

		HttpsURLConnection connection = setupConnection(INDEX_URL);

        List<TermCategory> results = new ArrayList<>();
		int responseCode = connection.getResponseCode();
		if (responseCode == 204) {
			return results;
		} else if (responseCode >= 200 && responseCode < 300) {
			String input;
			BufferedReader in = new BufferedReader(
					new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String totalInput = "";
            while ((input = in.readLine()) != null) {
                totalInput += input;
            }
            JSONArray jsonArray = new JSONArray(totalInput);
            if (jsonArray.length() > 0) {
                for (int index = 0; index < jsonArray.length(); index++) {
                    JSONObject object = jsonArray.getJSONObject(index);
                    TermCategory termCategory = TermCategory.from(object);
                    results.add(termCategory);
                }
            }
            in.close();
		} else {
            throw new IOException("Could not read server index");
		}
		return results;
	}

	private HttpsURLConnection setupConnection(String forURL) throws IOException {
		URL url = new URL(forURL);
		HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
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
