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
			String termsUrl = rs.getString("termsUrl");
			long updated = rs.getLong("updated");
			TermCategory category = new TermCategory();
			category.id = id;
			category.nameEn = nameEn;
			category.nameFi = nameFi;
			category.nameSe = nameSe;
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
			String termsUrl = rs.getString("termsUrl");
			long updated = rs.getLong("updated");
			category = new TermCategory();
			category.id = id;
			category.nameEn = nameEn;
			category.nameFi = nameFi;
			category.nameSe = nameSe;
			category.termsURL = termsUrl;
			category.updated = LocalDateTime.ofInstant(Instant.ofEpochMilli(updated), ZoneOffset.UTC);
		}
		queryStatement.close();
		return category;
	}

	public void saveCategories(List<TermCategory> categories) throws SQLException {
		logger.debug("Saving categories to db");
		String insertMsgStatement = "insert into category (id, nameEn, nameFi, nameSe, termsUrl, updated)"
				+ " values(?, ?, ?, ?, ?, ?) on conflict (id) do update" + " set nameEn = excluded.nameEn,"
				+ " nameFi = excluded.nameFi," + " nameSe = excluded.nameSe," + " termsUrl = excluded.termsUrl";
		PreparedStatement createStatement;
		createStatement = connection.prepareStatement(insertMsgStatement);
		for (TermCategory category : categories) {
			createStatement.setString(1, category.id);
			createStatement.setString(2, category.nameEn);
			createStatement.setString(3, category.nameFi);
			createStatement.setString(4, category.nameSe);
			createStatement.setString(5, category.termsURL);
			var timeStamp = category.updated.toInstant(ZoneOffset.UTC).toEpochMilli();
			createStatement.setLong(6, timeStamp);
			createStatement.executeUpdate();
		}
		createStatement.close();
	}

	public List<Term> readTerms(String forCategoryId) throws SQLException {
		logger.debug("Reading terms for a category from db: " + forCategoryId);
		List<Term> terms = new ArrayList<>();
		String query = "select * from term where category = ?";
		Term term = null;
		PreparedStatement queryStatement = connection.prepareStatement(query);
		queryStatement.setString(1, forCategoryId);
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
			String createUsersString = "create table category " + "(id varchar(32) NOT NULL, "
					+ "nameEn varchar(32) NOT NULL, " + "nameFi varchar(32) NOT NULL, " + "nameSe varchar(32) NOT NULL, "
					+ "termsUrl varchar(32) NOT NULL, " + "updated integer NOT NULL, " + "PRIMARY KEY (id))";
			Statement createStatement = connection.createStatement();
			createStatement.executeUpdate(createUsersString);
			createStatement.close();
			createStatement = connection.createStatement();
			String createChatsString = "create table term " + "(id varchar(32) NOT NULL, "
					+ "english varchar(32) NOT NULL, " + "finnish varchar(32) NOT NULL, "
					+ "englishLink varchar(64) NOT NULL, " + "finnishLink varchar(64) NOT NULL, "
					+ "definition varchar(1000) NOT NULL, " + "category varchar(32) NOT NULL, "
					+ "PRIMARY KEY(id,category), "
					+ "FOREIGN KEY (category) REFERENCES category (id) ON UPDATE CASCADE ON DELETE CASCADE)";
			createStatement.executeUpdate(createChatsString);
			createStatement.close();
			return true;
		}
		logger.warn("db connection was null, db not initialized");
		return false;
	}

}
