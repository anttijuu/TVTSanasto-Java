package fi.oulu.tol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Properties;

import fi.oulu.tol.model.Language;

// TODO: put the index JSON URL in settings so it can be changed?
//   Default index URL hardcoded here.

public class Settings {
	private Settings() {
	}

	public static int WINDOW_WIDTH = 1200;
	public static int WINDOW_HEIGHT = 800;
	public static int LIST_WIDTH = 250;
	public static Language language = Language.FINNISH;
	public static LocalDateTime lastIndexFetchDateTime = null;
	private static final String DEFAULT_JSON_INDEX_URL = "https://gitlab.com/sanasto/index/-/raw/main/index.json";
	public static String mainIndexJSONFileURL = null;

	private static final String CONFIGURATION_FILE_NAME = "settings.properties";

	public static void readSettings() {
		File configFile = new File(CONFIGURATION_FILE_NAME);
		Properties config = new Properties();
		try(FileInputStream istream = new FileInputStream(configFile)) {
			config.load(istream);
			long indexUpdatedTimeStamp = Long.parseLong(config.getProperty("indexupdated", "557442000000"));
			lastIndexFetchDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(indexUpdatedTimeStamp), ZoneOffset.UTC);
			String lang = config.getProperty("sortorder", "fi");
			if (lang.equalsIgnoreCase("fi")) {
				language = Language.FINNISH;
			} else if (lang.equalsIgnoreCase("en")) {
				language = Language.ENGLISH;
			}
			String indexUrl = config.getProperty("indexURL", DEFAULT_JSON_INDEX_URL);
			// Not a good way to validate URLs but catches at least something.
			URL url = new URL(indexUrl);
			mainIndexJSONFileURL = indexUrl;
		} catch (MalformedURLException e) {
			System.out.println("Malformed index URL in settings, aborting");
			e.printStackTrace();
			System.exit(42);
		} catch (IOException e) {
			language = Language.FINNISH;
			lastIndexFetchDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(557442000000L), ZoneOffset.UTC);
			mainIndexJSONFileURL = DEFAULT_JSON_INDEX_URL;
		}
	}

	public static void saveSettings() {
		File configFile = new File(CONFIGURATION_FILE_NAME);
		Properties config = new Properties();
		try(FileOutputStream ostream = new FileOutputStream(configFile)) {
			if (null != lastIndexFetchDateTime) {
				long updated = lastIndexFetchDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
				config.setProperty("indexupdated", Long.toString(updated));
			}
			if (language == Language.FINNISH) {
				config.setProperty("sortorder", "fi");
			} else if (language == Language.ENGLISH) {
				config.setProperty("sortorder", "en");
			}
			config.store(ostream, "TVT Sanasto asetukset");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
