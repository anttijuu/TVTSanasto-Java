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

public class LocalDatabase {
    
    private Connection connection = null;

    public void open(String dbName) throws SQLException {
		boolean createDatabase = false;
		File file = new File(dbName);
		if (!file.exists() && !file.isDirectory()) {
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
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			connection = null;
		}
	}

    public List<TermCategory> readCategories() throws SQLException {
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
		queryStatement.close();
        return categories;
    }

    public TermCategory readCategory(String withID) throws SQLException {
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
		String insertMsgStatement = "insert into category values(?, ?, ?, ?, ?, ?)";
		PreparedStatement createStatement;
		createStatement = connection.prepareStatement(insertMsgStatement);
		createStatement.setString(1, user);
		createStatement.setString(2, message.nick);
		createStatement.setLong(3, message.dateAsLong());
		createStatement.setString(4, message.message);
		createStatement.executeUpdate();
		createStatement.close();
    }
    
    private boolean initializeDatabase() throws SQLException {
		if (null != connection) {
			String createUsersString = "create table category " + 
					"(id varchar(32) NOT NULL, " +
					"nameEn varchar(32) NOT NULL, " +
					"nameFi varchar(32) NOT NULL, " +
					"nameSe varchar(32) NOT NULL, " +
					"termsUrl varchar(32) NOT NULL, " +
                    "updated integer NOT NULL, " +
					"PRIMARY KEY (id))";
			Statement createStatement = connection.createStatement();
			createStatement.executeUpdate(createUsersString);
			createStatement.close();
			createStatement = connection.createStatement();
			String createChatsString = "create table term " +
					"(id varchar(32) NOT NULL, " +
					"english varchar(32) NOT NULL, " +
					"finnish varchar(32) NOT NULL, " +
					"englishLink varchar(64) NOT NULL, " +
					"finnishLink varchar(64) NOT NULL, " +
					"definition varchar(1000) NOT NULL, " +
					"category varchar(32) NOT NULL, " +
					"PRIMARY KEY(id,category), " + 
                    "FOREIGN KEY (category) REFERENCES category (id) ON UPDATE CASCADE ON DELETE CASCADE)";
			createStatement.executeUpdate(createChatsString);
			createStatement.close();
			return true;
		}
		return false;
	}

}
