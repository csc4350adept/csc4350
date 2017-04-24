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
		if (c == null) {
			throw new SQLException("SQL connection failed.");
		}
		return c;
	}

	/*
	 * Retrieves a password from the database using a username
	 */
	public static String getPassword (String username) {
		String resp = null;
		//Gets database connection "c"
		java.sql.Connection c;
		System.out.println("dfa");
		try {
			c = createDB();
		} catch (SQLException e) {
			System.out.println("dfafd");
			return null;
		}
		//Constructs SQL string
		String sql = String.format("select password from users where email='%s'", username);
		//Executes query
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery(sql);
			System.out.println("dfsafasd");
			//If there is a row returned, get the column "passwordhash" and return it
			if (rs.next() && rs.getString("password") != null) {
				resp = rs.getString("password");
				System.out.println("Password: " + resp);
			}
		} catch (SQLException e) {
			/* nothing */
			System.out.println(e.getMessage());
		}
		//If there was a SQLException or no password, return null
		try {
			c.close();
		} catch (SQLException e) {
			/* nothing */
		}
		System.out.println("Password: " + resp);
		return resp;
	}
	
	
	/*
	 * Take in a user name
	 * Return an ArrayList<String> of mailbox names that belong to that user
	 * Modify email table so that every email belongs to a folder
	 * Create table mailboxes with columns <email, mailboxName> with both as primary keys 
	 */
	public static ArrayList<String> listRef(String userName) {
		ArrayList<String> resp = new ArrayList<String>();
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return null;
		}
		//Constructs SQL string
		String sql = String.format("select mailbox from mailboxes inner join users on mailboxes.owner=users.user_id where email='%s'", userName);
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
			resp = mailboxes;
		} catch (SQLException e) {
			/* nothing */
		}
		//If there was a SQLException or no password, return null
		try {
			c.close();
		} catch (SQLException e) {
			/* nothing */
		}
		return resp;
	}
	
	/*
	 * Take in a email and a mailboxName
	 * Return an ArrayList of emailIDs that belong to that email and belong to that mailboxName
	 */
	public static ArrayList<String> listMailbox(String userName, String mailboxName) {
		ArrayList<String> resp = new ArrayList<String>();
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return null;
		}
		//Constructs SQL string
		String sql = String.format("select email_id from emails inner join users on emails.owner=users.user_id inner join mailboxes on emails.mailbox=mailboxes.mailbox_id where email='%s' and mailboxes.mailbox='%s'", userName, mailboxName);
		//Executes query
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery(sql);
			System.out.println("Executed query: " + sql);
			ArrayList<String> mailboxes = new ArrayList<String>();
			//If there is a row returned, add to the mailboxes list
			String mailbox;
			while (rs.next() && (mailbox = rs.getString("email_id")) != null) {
				mailboxes.add(mailbox);
			}
			resp = mailboxes;
		} catch (SQLException e) {
			/* nothing */
		}
		//If there was a SQLException or no password, return null
		try {
			c.close();
		} catch (SQLException e) {
			/* nothing */
		}
		return resp;
	}
	
	/*
	 * Take in a email and emailID
	 * Return a HashMap<String, String> with all the data for the email that has that emailID and belongs to that email
	 */
	public static HashMap<String, String> fetch(String emailID, String fetchType) {
		HashMap<String, String> resp = null;
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
		if (fetchType.equals("BODY[HEADER]"))
			parts.addAll(Arrays.asList(new String[] {"date", "to", "from", "subject"}));
		else if (fetchType.equals("BODY[TEXT]"))
			parts.add("body");
		else if (fetchType.equals("FLAGS"))
			parts.add("read");
		else {
			try {
				c.close();
			} catch (SQLException e) {
				/* do nothing */
			}
			return null;
		}
		HashMap<String, String> partsFormatted = new HashMap<String, String>();
		ArrayList<String> partsSQL = new ArrayList<String>();
		for (String part : parts) {
			String partSQL = String.format("emails.%s", part);
			partsFormatted.put(part, partSQL);
			partsSQL.add(partSQL);
		}
		sql = String.format("select %s from emails where email_id=%s", String.join(", ", partsSQL), emailID);
		
		//Executes query
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery(sql);
			System.out.println("Executed query: " + sql);
			
			while (rs.next()) {
				String component;
				for (String part : parts) {
					component = rs.getString(part);
					if (part.equals("read")) {
						switch (component) {
							case "t":
								component = "READ";
								break;
							case "f":
								component = "UNREAD";
								break;
						}
					}
					email.put(part, component);
				}
			}
			if (email.keySet().size() > 0) {
				resp = email;
			}
		} catch (SQLException e) {
			/* nothing */
		}
		//If there was a SQLException or no password, return null
		try {
			c.close();
		} catch (SQLException e) {
			/* nothing */
		}
		return resp;
	}
	
	public static boolean setRead(String emailID) {
		boolean resp = false;
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return false;
		}
		//Constructs SQL string
		String sql = String.format("update emails set read=true where email_id=%s", emailID);
		//Executes query
		try {
			Statement st = c.createStatement();
			st.executeQuery(sql);
			System.out.println("Executed query: " + sql);
		} catch (SQLException e) {
			if (e.getMessage().startsWith("No results were returned by the query")) resp = true;
			else System.out.println(e.getMessage());
		}
		//If there was a SQLException or no password, return null
		try {
			c.close();
		} catch (SQLException e) {
			/* nothing */
		}
		return resp;
	}
	
	public static boolean moveEmail(String ownerName, String emailId, String mailbox) {
		boolean resp = false;
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return false;
		}
		
		String mailboxId = getMailboxId(mailbox, ownerName);
		System.out.println(mailboxId);
		//Constructs SQL string
		String sql = String.format("update emails set mailbox=%s where email_id=%s", mailboxId, emailId);
		//Executes query
		try {
			Statement st = c.createStatement();
			st.executeQuery(sql);
			System.out.println("Executed query: " + sql);
		} catch (SQLException e) {
			if (e.getMessage().startsWith("No results were returned by the query")) resp = true;
			else System.out.println(e.getMessage());
		}
		try {
			c.close();
		} catch (SQLException e) {
			/* nothing */
		}
		return resp;
	}
	
	public static boolean emailExists(String email) {
		boolean resp = false;
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return false;
		}
		//Constructs SQL string
		String sql = String.format("select user_id from users where email='%s'", email);
		//Executes query
		int count = 0;
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery(sql);
			System.out.println("Executed query: " + sql);
			while (rs.next())
				count++;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		if (count == 1) resp = true;
		try {
			c.close();
		} catch (SQLException e) {
			/* nothing */
		}
		return resp;
	}
	
	public static String getEmailId(String email) {
		String resp = null;
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return null;
		}
		//Constructs SQL string
		String sql = String.format("select user_id from users where email='%s'", email);
		
		//Executes query
		try {
			Statement st = c.createStatement();
			System.out.println("Executed query: " + sql);
			ResultSet rs = st.executeQuery(sql);
			ArrayList<String> ids = new ArrayList<String>();
			//If there is a row returned, add to the mailboxes list
			String id;
			while (rs.next() && (id = rs.getString("user_id")) != null) {
				ids.add(id);
			}
			if (ids.size() == 1) {
				resp = ids.get(0);
			}
		} catch (SQLException e) {
			/* nothing */
		}
		//If there was a SQLException or no password, return null
		try {
			c.close();
		} catch (SQLException e) {
			/* nothing */
		}
		return resp;
	}
	
	public static String getEmailFromId(String emailId) {
		String resp = null;
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return null;
		}
		//Constructs SQL string
		String sql = String.format("select email from users where email_id='%s'", emailId);
		//Executes query
		try {
			Statement st = c.createStatement();
			System.out.println("Executed query: " + sql);
			ResultSet rs = st.executeQuery(sql);
			ArrayList<String> ids = new ArrayList<String>();
			//If there is a row returned, add to the mailboxes list
			String id;
			while (rs.next() && (id = rs.getString("email")) != null) {
				ids.add(id);
			}
			if (ids.size() == 1) resp = ids.get(0);
		} catch (SQLException e) {
			/* nothing */
		}
		//If there was a SQLException or no password, return null
		try {
			c.close();
		} catch (SQLException e) {
			/* nothing */
		}
		return resp;
	}
	
	public static String getMailboxId(String mailbox, String owner) {
		String resp = null;
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return null;
		}
		//Constructs SQL string
		String sql = String.format("select mailbox_id from mailboxes inner join users on mailboxes.owner=users.user_id where mailbox='%s' and email='%s'", mailbox, owner);
		//Executes query
		try {
			Statement st = c.createStatement();
			System.out.println("Executed query: " + sql);
			ResultSet rs = st.executeQuery(sql);
			ArrayList<String> mailboxes = new ArrayList<String>();
			//If there is a row returned, add to the mailboxes list
			String m;
			while (rs.next() && (m = rs.getString("mailbox_id")) != null) {
				System.out.println("dfsafdas" + m);
				mailboxes.add(m);
			}
			if (mailboxes.size() == 1) resp = mailboxes.get(0);
		} catch (SQLException e) {
			/* nothing */
			System.out.println(e.getMessage());
		}
		//If there was a SQLException or no password, return null
		try {
			c.close();
		} catch (SQLException e) {
			/* nothing */
		}
		return resp;
	}
	
	public static boolean createMailbox(String owner, String mailbox) {
		if (mailboxExists(owner, mailbox)) return true;
		boolean resp = false;
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return false;
		}
		
		String user_id = getEmailId(owner);
		
		//Constructs SQL string
		String sql = String.format("insert into mailboxes values(default, '%s', %s)", mailbox, user_id);
		//Executes query
		try {
			if (user_id == null) {
				try {
					c.close();
				} catch (SQLException e) {
					/* nothing */
				}
				return false;
			}
			Statement st = c.createStatement();
			System.out.println("Executed query: " + sql);
			st.executeQuery(sql);
		} catch (SQLException e) {
			if (e.getMessage().startsWith("No results were returned by the query")) resp = true;
			else System.out.println(e.getMessage());
		}
		//If there was a SQLException or no password, return null
		try {
			c.close();
		} catch (SQLException e) {
			/* nothing */
		}
		return resp;
	}
	
	public static boolean renameMailbox(String owner, String mailbox, String newname) {
		if (mailboxExists(owner, mailbox)) return true;
		boolean resp = false;
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return false;
		}
		
		String user_id = getEmailId(owner);
		
		
		//Constructs SQL string
		String sql = String.format("update mailboxes set mailbox='%s' where mailbox='%s' and owner=%s", newname, mailbox, user_id);
		//Executes query
		try {
			if (user_id == null) {
				try {
					c.close();
				} catch (SQLException e) {
					/* nothing */
				}
				return false;
			}
			Statement st = c.createStatement();
			System.out.println("Executed query: " + sql);
			st.executeQuery(sql);
		} catch (SQLException e) {
			if (e.getMessage().startsWith("No results were returned by the query")) resp = true;
			else System.out.println(e.getMessage());
		}
		//If there was a SQLException or no password, return null
		try {
			c.close();
		} catch (SQLException e) {
			/* nothing */
		}
		return resp;
	}
	
	public static boolean deleteMailbox(String owner, String mailbox) {
		if (!mailboxExists(owner, mailbox)) return true;
		boolean resp = false;
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return false;
		}
		
		String user_id = getEmailId(owner);
		
		//Constructs SQL string
		String sql = String.format("delete from mailboxes where mailbox='%s' and owner=%s", mailbox, user_id);
		//Executes query
		try {
			if (user_id == null) {
				try {
					c.close();
				} catch (SQLException e) {
					/* nothing */
				}
				return false;
			}
			Statement st = c.createStatement();
			System.out.println("Executed query: " + sql);
			st.executeQuery(sql);
		} catch (SQLException e) {
			if (e.getMessage().startsWith("No results were returned by the query")) resp = true;
			else System.out.println(e.getMessage());
		}
		//If there was a SQLException or no password, return null
		try {
			c.close();
		} catch (SQLException e) {
			/* nothing */
		}
		return resp;
	}
	
	public static boolean deleteEmail(String email) {
		boolean resp = false;
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return false;
		}
		
		//Constructs SQL string
		String sql = String.format("delete from emails where email_id=%s", email);
		//Executes query
		try {
			Statement st = c.createStatement();
			System.out.println("Executed query: " + sql);
			st.executeQuery(sql);
		} catch (SQLException e) {
			if (e.getMessage().startsWith("No results were returned by the query")) resp = true;
			else System.out.println(e.getMessage());
		}
		//If there was a SQLException or no password, return null
		try {
			c.close();
		} catch (SQLException e) {
			/* nothing */
		}
		return resp;
	}
	
	public static boolean mailboxExists(String owner, String mailbox) {
		boolean resp = false;
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return false;
		}
		//Constructs SQL string
		String sql = String.format("select mailbox_id from mailboxes inner join users on mailboxes.owner=users.user_id where mailbox='%s' and email='%s'", mailbox, owner);
		//Executes query
		int count = 0;
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery(sql);
			System.out.println("Executed query: " + sql);
			while (rs.next())
				count++;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		if (count == 1) resp = true;
		try {
			c.close();
		} catch (SQLException e) {
			/* nothing */
		}
		return resp;
	}

	public static boolean receiveEmail(ArrayList<String> owners, HashMap<String, String> email) {
		boolean success = true;
		
		//Gets database connection "c"
		java.sql.Connection c;
		try {
			c = createDB();
		} catch (SQLException e) {
			return false;
		}
		ArrayList<String> noQuotes = new ArrayList<String>(Arrays.asList(new String[] {"email_id", "owner", "read", "mailbox", "date"}));
		email.put("email_id", "default");
		email.put("mailbox", "inbox");
		email.put("read", "false");
		email.put("date", "now()");
		
		ArrayList<String> requiredFields = new ArrayList<String>(Arrays.asList(new String[] {"email_id", "to", "from", "body", "date", "read", "subject", "mailbox"}));
			//Owner gets added a few lines down so don't check it here
		boolean valid = true;
		for (String field : requiredFields)
			if (!email.keySet().contains(field)) valid = false;
		if (!valid) {
			System.out.println("Invalid email");
			System.out.println("Required Fields: " + String.join(",", requiredFields));
			System.out.println("Included Fields: " + String.join(",", email.keySet()));
			try {
				c.close();
			} catch (SQLException e) {
				/* nothing */
			}
			return false;
		}
		
		for (String owner : owners) {
			email.put("owner", owner);
			
			if (!mailboxExists(owner, email.get("mailbox"))) createMailbox(owner, email.get("mailbox")); //Everyone needs an inbox
			
			
			
			//Constructs SQL string
			ArrayList<String> parts = new ArrayList<String>(email.keySet());
			ArrayList<String> values = new ArrayList<String>();
			String partsString;
			String valuesString;
			for (String part : parts) {
				String value = email.get(part);
				if (noQuotes.contains(part)) {
					switch (part) {
					case "mailbox":
						System.out.println("mailbox is " + value);
						value = getMailboxId(value, owner);
						if (value == null) {
							System.out.println("Missing mailbox id for " + value + ", " + owner);
							try {
								c.close();
							} catch (SQLException e) {
								/* nothing */
							}
							return false;
						}
						break;
					case "owner":
						value = getEmailId(value);
						if (value == null) {
							System.out.println("Missing email id for " + value);
							try {
								c.close();
							} catch (SQLException e) {
								/* nothing */
							}
							return false;
						}
						break;
					}
					values.add(value);
				}
				else {
					System.out.println(part + ", " + value);
					values.add("'" + value.replaceAll("'", "''") + "'");
				}
			}
			ArrayList<String> tmp = new ArrayList<String>();
			for (String part : parts) tmp.add("\""+part+"\"");
			parts = tmp;
			partsString = String.join(", ", parts);
			valuesString = String.join(", ", values);
			String sql = String.format("insert into emails (%s) values (%s);", partsString, valuesString);
			
			
			//Executes query
			try {
				Statement st = c.createStatement();
				System.out.println("Executed query: " + sql);
				st.executeQuery(sql);
			} catch (SQLException e) {
				if (!e.getMessage().startsWith("No results were returned by the query")) {
					success = false;
					System.out.println(e.getMessage());
				}
			}
		}
		try {
			c.close();
		} catch (SQLException e) {
			/* nothing */
		}
		return success;
	}
	
}
