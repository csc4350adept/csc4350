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
		String sql = String.format("select password from users where email='%s'", username);
		//Executes query
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery(sql);
			System.out.println("Executed query: " + sql);
			String pword;
			//If there is a row returned, get the column "passwordhash" and return it
			if (rs.next() && rs.getString("password") != null) {
				pword = rs.getString("password");
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
	public static ArrayList<String> listRef(String userName) {
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return null;
		}
		//Constructs SQL string
		String sql = String.format("select mailbox from mailboxes where owner='%s'", userName);
		//Executes query
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery(sql);
			System.out.println("Executed query: " + sql);
			ArrayList<String> mailboxes = new ArrayList<String>();
			//If there is a row returned, add to the mailboxes list
			String mailbox;
			while (rs.next() && (mailbox = rs.getString("mailbox")) != null) {
				mailboxes.add(mailbox);
			}
			System.out.println("Got database response\n" + String.join("\n", mailboxes));
			return mailboxes;
		} catch (SQLException e) {
			/* nothing */
		}
		//If there was a SQLException or no password, return null
		return null;
	}
	
	/*
	 * Take in a email and a mailboxName
	 * Return an ArrayList of emailIDs that belong to that email and belong to that mailboxName
	 */
	public static ArrayList<String> listMailbox(String userName, String mailboxName) {
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return null;
		}
		//Constructs SQL string
		String sql = String.format("select email_id from emails inner join users on emails.owner=users.user_id where email='%s' and mailbox='%s';", userName, mailboxName);
		//Executes query
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery(sql);
			System.out.println("Executed query: " + sql);
			ArrayList<String> mailboxes = new ArrayList<String>();
			//If there is a row returned, add to the mailboxes list
			String mailbox;
			while (rs.next() && (mailbox = rs.getString("mailbox")) != null) {
				mailboxes.add(mailbox);
			}
			return mailboxes;
		} catch (SQLException e) {
			/* nothing */
		}
		//If there was a SQLException or no password, return null
		return null;
	}
	
	/*
	 * Take in a email and emailID
	 * Return a HashMap<String, String> with all the data for the email that has that emailID and belongs to that email
	 */
	public static HashMap<String, String> fetch(String emailID, String fetchType) {
		HashMap<String, String> email = new HashMap<String, String>();
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return null;
		}
		//Constructs SQL string
		String sql;
		ArrayList<String> parts = new ArrayList<String>();
		if (fetchType.equals("HEADER"))
			parts.addAll(Arrays.asList(new String[] {"date", "to", "from", "subject"}));
		else if (fetchType.equals("BODY"))
			parts.add("body");
		else if (fetchType.equals("FLAGS"))
			parts.add("read");
		else
			return null;
		sql = String.format("select %s from emails where email_id=%s", String.join(", ", parts), emailID);
		//Executes query
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery(sql);
			System.out.println("Executed query: " + sql);
			String component;
			for (String part : parts) {
				if (rs.next() && (component = rs.getString(part)) != null) {
					if (part.equals("read")) {
						switch (component) {
							case "t":
								component = "READ";
							case "f":
								component = "UNREAD";
						}
					}
					email.put(part, component);
				}
			}
			if (email.keySet().size() > 0) return email;
		} catch (SQLException e) {
			/* nothing */
		}
		//If there was a SQLException or no password, return null
		return null;
	}
	
	/*
	 * We probably don't need this method
	 */
	public static boolean updateAll(HashMap<String, HashMap<String, String>> mailboxEmails) {
		return true;
	}
	
}
