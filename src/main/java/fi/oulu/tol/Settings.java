package fi.oulu.tol;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Properties;

import fi.oulu.tol.model.Language;

public class Settings {
	private Settings() {
	}

	public static int WINDOW_WIDTH = 1200;
	public static int WINDOW_HEIGHT = 800;
	public static int LIST_WIDTH = 250;
	public static Language language = Language.FINNISH;
	public static LocalDateTime lastIndexFetchDateTime = null;

	private static final String configFileName = "settings.properties";

	public static void readSettings() {
		File configFile = new File(configFileName);
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
		} catch (IOException e) {
			language = Language.FINNISH;
			lastIndexFetchDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(557442000000L), ZoneOffset.UTC);
		}
	}

	public static void saveSettings() {
		File configFile = new File(configFileName);
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
