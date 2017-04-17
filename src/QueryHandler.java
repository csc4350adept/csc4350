import java.util.ArrayList;
import java.util.HashMap;

public class QueryHandler {
	//TODO Implement DB stuff here
	//Expected Result 1: "???? OK - User Authenticated" if username and password are the same
	//Expected Result 2: "???? NO - Login Failure: Invalid username or password
	//Expected Result 3: "???? If the query fails, return null"
	
	public static String getPassword (String username) {
		return "foobar"; // The user's actual password in the DB 
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
