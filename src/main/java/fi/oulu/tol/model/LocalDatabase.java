package fi.oulu.tol.model;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocalDatabase {

	private Connection connection = null;
	/**
	 * CURRENT_SCHEMA_VERSION is the current app database schema version.
	 * If you change the database schema after releasing the app to users,
	 * you should do that in updateDatabaseSchema(), not in initializeDatabase(). 
	 * initializeDatabase() should be used only when the installation
	 * does not have a database file at all.
	 */
	private static final int CURRENT_SCHEMA_VERSION = 1;

	private static final Logger logger = LogManager.getLogger(LocalDatabase.class);

	public void open(String dbName) throws SQLException {
		logger.info("Openging a database file " + dbName);
		boolean createDatabase = false;
		File file = new File(dbName);
		if (!file.exists() && !file.isDirectory()) {
			logger.info("Db file does not exist, create it");
			createDatabase = true;
		}
		String database = "jdbc:sqlite:" + dbName;
		connection = DriverManager.getConnection(database);
		if (createDatabase) {
			initializeDatabase();
		}
		updateDatabaseSchema();
	}

	public void close() {
		if (null != connection) {
			try {
				logger.debug("Closing the db connection");
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			connection = null;
		}
	}

	public List<TermCategory> readCategories() throws SQLException {
		logger.debug("Reading all categories from db");
		List<TermCategory> categories = new ArrayList<>();
		String queryMessages = "select * from category";
		Statement queryStatement = connection.createStatement();
		ResultSet rs = queryStatement.executeQuery(queryMessages);
		while (rs.next()) {
			String id = rs.getString("id");
			String nameEn = rs.getString("nameEn");
			String nameFi = rs.getString("nameFi");
			String nameSe = rs.getString("nameSe");
			String aboutUrl = rs.getString("aboutUrl");
			String termsUrl = rs.getString("termsUrl");
			long updated = rs.getLong("updated");
			TermCategory category = new TermCategory();
			category.id = id;
			category.nameEn = nameEn;
			category.nameFi = nameFi;
			category.nameSe = nameSe;
			category.aboutURL = aboutUrl;
			category.termsURL = termsUrl;
			category.updated = LocalDateTime.ofInstant(Instant.ofEpochMilli(updated), ZoneOffset.UTC);
			categories.add(category);
		}
		logger.debug(String.format("Read %d categories", categories.size()));
		queryStatement.close();
		return categories;
	}

	public TermCategory readCategory(String withID) throws SQLException {
		logger.debug("Reading a single category from db");
		String query = "select * from category where id = ?";
		TermCategory category = null;
		PreparedStatement queryStatement = connection.prepareStatement(query);
		queryStatement.setString(1, withID);
		ResultSet rs = queryStatement.executeQuery();
		if (rs.next()) {
			String id = rs.getString("id");
			String nameEn = rs.getString("nameEn");
			String nameFi = rs.getString("nameFi");
			String nameSe = rs.getString("nameSe");
			String aboutUrl = rs.getString("aboutUrl");
			String termsUrl = rs.getString("termsUrl");
			long updated = rs.getLong("updated");
			category = new TermCategory();
			category.id = id;
			category.nameEn = nameEn;
			category.nameFi = nameFi;
			category.nameSe = nameSe;
			category.aboutURL = aboutUrl;
			category.termsURL = termsUrl;
			category.updated = LocalDateTime.ofInstant(Instant.ofEpochMilli(updated), ZoneOffset.UTC);
		}
		queryStatement.close();
		return category;
	}

	public void saveCategories(List<TermCategory> categories) throws SQLException {
		logger.debug("Saving categories to db");
		String insertMsgStatement = "insert into category (id, nameEn, nameFi, nameSe, aboutUrl, termsUrl, updated)"
				+ " values(?, ?, ?, ?, ?, ?, ?) on conflict (id) do update" + " set nameEn = excluded.nameEn,"
				+ " nameFi = excluded.nameFi," + " nameSe = excluded.nameSe," + " aboutUrl = excluded.aboutUrl"
				+ " + termsUrl = excluded.termsUrl";
		PreparedStatement createStatement;
		createStatement = connection.prepareStatement(insertMsgStatement);
		for (TermCategory category : categories) {
			createStatement.setString(1, category.id);
			createStatement.setString(2, category.nameEn);
			createStatement.setString(3, category.nameFi);
			createStatement.setString(4, category.nameSe);
			createStatement.setString(5, category.aboutURL);
			createStatement.setString(6, category.termsURL);
			var timeStamp = category.updated.toInstant(ZoneOffset.UTC).toEpochMilli();
			createStatement.setLong(7, timeStamp);
			createStatement.executeUpdate();
		}
		createStatement.close();
	}

	public List<Term> readAllTerms() throws SQLException {
		logger.debug("Reading all terms from db");
		List<Term> terms = new ArrayList<>();
		String query = "select * from term";
		Term term = null;
		PreparedStatement queryStatement = connection.prepareStatement(query);
		ResultSet rs = queryStatement.executeQuery();
		while (rs.next()) {
			String id = rs.getString("id");
			String english = rs.getString("english");
			String finnish = rs.getString("finnish");
			String englishLink = rs.getString("englishLink");
			String finnishLink = rs.getString("finnishLink");
			String definition = rs.getString("definition");
			term = new Term();
			term.id = id;
			term.english = english;
			term.finnish = finnish;
			term.englishLink = englishLink;
			term.finnishLink = finnishLink;
			term.definition = definition;
			terms.add(term);
		}
		queryStatement.close();
		return terms;
	}

	public List<Term> readTerms(String forCategoryId, Language inOrder) throws SQLException {
		logger.debug("Reading terms for a category from db: " + forCategoryId);
		List<Term> terms = new ArrayList<>();
		String query = "select * from term where category = ? order by ?";
		Term term = null;
		PreparedStatement queryStatement = connection.prepareStatement(query);
		queryStatement.setString(1, forCategoryId);
		queryStatement.setString(2, inOrder == Language.FINNISH ? "finnish asc" : "english asc");
		ResultSet rs = queryStatement.executeQuery();
		while (rs.next()) {
			String id = rs.getString("id");
			String english = rs.getString("english");
			String finnish = rs.getString("finnish");
			String englishLink = rs.getString("englishLink");
			String finnishLink = rs.getString("finnishLink");
			String definition = rs.getString("definition");
			term = new Term();
			term.id = id;
			term.english = english;
			term.finnish = finnish;
			term.englishLink = englishLink;
			term.finnishLink = finnishLink;
			term.definition = definition;
			terms.add(term);
		}
		queryStatement.close();
		return terms;
	}

	public void saveTerms(List<Term> terms, TermCategory category) throws SQLException {
		logger.debug("Saving terms for a category to db");
		String insertTermStatement = "insert into term (id, english, finnish, englishLink, finnishLink, definition, category)"
				+ " values(?, ?, ?, ?, ?, ?, ?) on conflict (id, category) do update set english = excluded.english,"
				+ " finnish = excluded.finnish, englishLink = excluded.englishLink, finnishLink = excluded.finnishLink, definition = excluded.definition";
		PreparedStatement createStatement;
		createStatement = connection.prepareStatement(insertTermStatement);
		for (Term term : terms) {
			createStatement.setString(1, term.id);
			createStatement.setString(2, term.english);
			createStatement.setString(3, term.finnish);
			createStatement.setString(4, term.englishLink);
			createStatement.setString(5, term.finnishLink);
			createStatement.setString(6, term.definition);
			createStatement.setString(7, category.id);
			createStatement.executeUpdate();
		}
		createStatement.close();
		logger.debug("Updating the category update datatime to db");
		String updateStatement = "update category set updated = ? where id = ?";
		PreparedStatement update = connection.prepareStatement(updateStatement);
		update.setLong(1, category.updated.toInstant(ZoneOffset.UTC).toEpochMilli());
		update.setString(2, category.id);
		update.executeUpdate();
		update.close();
	}

	private boolean initializeDatabase() throws SQLException {
		logger.info("Initializing the database tables");
		if (null != connection) {
			String createCategoryTable = "create table category " + "(id varchar(32) NOT NULL, "
					+ "nameEn varchar(32) NOT NULL, " + "nameFi varchar(32) NOT NULL, " + "nameSe varchar(32) NOT NULL, "
					+ "termsUrl varchar(32) NOT NULL, " + "updated integer NOT NULL, "
					+ "PRIMARY KEY (id))";
			Statement createStatement = connection.createStatement();
			createStatement.executeUpdate(createCategoryTable);
			createStatement.close();
			createStatement = connection.createStatement();
			String createTermTable = "create table term " + "(id varchar(32) NOT NULL, " + "english varchar(32) NOT NULL, "
					+ "finnish varchar(32) NOT NULL, " + "englishLink varchar(64) NOT NULL, "
					+ "finnishLink varchar(64) NOT NULL, " + "definition varchar(1000) NOT NULL, "
					+ "category varchar(32) NOT NULL, " + "PRIMARY KEY(id,category), "
					+ "FOREIGN KEY (category) REFERENCES category (id) ON UPDATE CASCADE ON DELETE CASCADE)";
			createStatement.executeUpdate(createTermTable);
			createStatement.close();
			return true;
		}
		logger.warn("db connection was null, db not initialized");
		return false;
	}

	private void updateDatabaseSchema() {
		logger.info("Checking if database schema needs updating");
		if (null != connection) {
			try (Statement statement = connection.createStatement()) {
				try (ResultSet rs = statement.executeQuery("PRAGMA user_version;")) {
					final int schemaVersion = rs.getInt(1);
					logger.info("PRAGMA user_version: " + schemaVersion);
					if (schemaVersion < CURRENT_SCHEMA_VERSION) {
						logger.info("db has older schema, updating!");
						////////////////////////////////
						// ADD ANY SCHEMA UPDATES HERE, lastest at the bottom!
						// Make sure you do this so that all updates are executed
						// if app has gone forward e.g. 4 database schema changes
						// but user's database is two steps behind. So in that 
						// case the latest two schema updates has to be executed.
						///////////////////////////////
						if (schemaVersion == 0) {
							logger.info("Updating from schema " + schemaVersion + " to version " + CURRENT_SCHEMA_VERSION);
							String addAboutURLStatement = "alter table category add aboutUrl varchar(32) NOT NULL default ''";
							try (Statement addAboutColumn = connection.createStatement()) {
								addAboutColumn.execute(addAboutURLStatement);
							}
							logger.info("Update successful");
						}
						// ADD next schema update here, after the previous one.

						// ========================================================
						// After all the schema updates have been done, update the
						// db schema to the app latest version in CURRENT_SCHEMA_VERSION.
						try(Statement updateSchemaStatement = connection.createStatement()) {
							logger.info("Updating schema version to db...");
							String pragmaExecute = "PRAGMA user_version = " + CURRENT_SCHEMA_VERSION;
							updateSchemaStatement.execute(pragmaExecute);
							logger.info("...updated schema version to db");
					  }
					} else {
						logger.info("No database update needed.");
					}
				}
			} catch (SQLException e) {
				logger.error("Error in checking/updating schema: " + e.getLocalizedMessage());
			}
		} else {
			logger.warn("No connection to database while checking for schema updates");
		}
	}
}
