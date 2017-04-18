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
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return null;
		}
		//Constructs SQL string
		String sql = String.format("select passwordhash from users where email='%s'", username);
		//Executes query
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery(sql);
			System.out.println("Executed query: " + sql);
			String pword;
			//If there is a row returned, get the column "passwordhash" and return it
			if (rs.next() && rs.getString("passwordhash") != null) {
				pword = rs.getString("passwordhash");
				System.out.println(String.format("Got this password from the DB: %s", rs.getString("passwordhash")));
				return pword;
			}
		} catch (SQLException e) {
			/* nothing */
		}
		//If there was a SQLException or no password, return null
		return null;
	}
	
	
	/*
	 * Take in a user name
	 * Return an ArrayList<String> of mailbox names that belong to that user
	 * Modify email table so that every email belongs to a folder
	 * Create table mailboxes with columns <email, mailboxName> with both as primary keys 
	 */
	public static ArrayList listRef(String userName) {
		ArrayList<String> folders = new ArrayList<String>();
		folders.add("inbox");
		folders.add("sent");
		folders.add("outbox");
		folders.add("bullshit");
		return folders; // The user's actual folders in the DB
	}
	
	/*
	 * Take in a email and a mailboxName
	 * Return an ArrayList of emailIDs that belong to that email and belong to that mailboxName
	 */
	public static ArrayList listMailbox(String userName, String mailboxName) {
		ArrayList<String> emailIDs = new ArrayList<String>();
		emailIDs.add("1");
		emailIDs.add("2");
		emailIDs.add("3");
		return emailIDs; // The user's actual email IDs in the DB
	}
	
	/*
	 * Take in a email and emailID
	 * Return a HashMap<String, String> with all the data for the email that has that emailID and belongs to that email
	 */
	public static HashMap emailID(String userName, String emailID) {
		HashMap<String, String> email = new HashMap<String, String>();
		email.put("owner", "ebull");
		email.put("subject", "its a stack of fuckshit");
		email.put("body", "on top of itself bitchhh");
		return email; // The user's actual email data corresponding to this.emailID, in the DB
	}
	
	/*
	 * We probably don't need this method
	 */
	public static boolean updateAll(HashMap<String, HashMap<String, String>> mailboxEmails) {
		return true;
	}
	
}
