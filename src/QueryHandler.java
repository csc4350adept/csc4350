import java.util.ArrayList;
import java.util.HashMap;
import java.sql.*;
import java.util.*;

/*
 * QueryHandler interacts with the server postgres database.
 * It will take in paramaters and run a select statement
 * It will return the results of the select statement if the select statement succeeds
 * If the select statement fails it will return null (or false if boolean)
 */
public class QueryHandler {
	
	/*
	 * This sets up the database connection and returns it.
	 */
	public static java.sql.Connection createDB() throws SQLException{
		java.sql.Connection c = null;
		String hostname = ServerController.getDBHostname();
		String port = ServerController.getDBPort();
		String dbname = ServerController.getDBName();
		String uname = ServerController.getDBUname();
		String pword = ServerController.getDBPwd();
		String driverManager = String.format("jdbc:postgresql://%s:%s/%s", hostname, port, dbname);
		try {
			c = DriverManager.getConnection(driverManager, uname, pword);
		} catch (SQLException e) {
			throw e;
		}
		if (c == null) throw new SQLException("SQL connection failed.");
		return c;
	}

	/*
	 * Retrieves a password from the database using a username
	 */
	public static String getPassword (String username) {
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return null;
		}
		String sql = String.format("select passwordhash from users where email = '%s'", username);
		try {
			Statement msg = c.createStatement();
			ResultSet resp = msg.executeQuery(sql);
			if (resp.getString("passwordhash") != null) return resp.getString("passwordhash");
		} catch (SQLException e) {
			/* nothing */
		}
		return null;
	}
	
	public static ArrayList listRef(String userName) {
		ArrayList<String> folders = new ArrayList<String>();
		folders.add("inbox");
		folders.add("sent");
		folders.add("outbox");
		folders.add("bullshit");
		return folders; // The user's actual folders in the DB
	}
	
	public static ArrayList listMailbox(String userName, String mailboxName) {
		ArrayList<String> emailIDs = new ArrayList<String>();
		emailIDs.add("1");
		emailIDs.add("2");
		emailIDs.add("3");
		return emailIDs; // The user's actual email IDs in the DB
	}
	
	public static HashMap emailID(String userName, String emailID) {
		HashMap<String, String> email = new HashMap<String, String>();
		email.put("owner", "ebull");
		email.put("subject", "its a stack of fuckshit");
		email.put("body", "on top of itself bitchhh");
		return email; // The user's actual email data corresponding to this.emailID, in the DB
	}
	
	public static boolean updateAll(HashMap<String, HashMap<String, String>> mailboxEmails) {
		return true;
	}
	
}
