import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

//By B. Daniel Garber

public class IMAPProcessor extends CmdProcessor {
	// Byte Processing Variables
	private String cmd;
	
	// Global Instances
	Query query;
	QueryHandler queryHandler;
	
	public IMAPProcessor() {
		query = new Query();
	}
	
	public String processBytes(byte[] command) {
		cmd = new String(processByteArray(command)); // For example "LOG USERNAME PASSWORD" format
		query.setCommand(cmd);
		
		return queryGenerator();
	}
	
	public boolean checkAuth(String username, String password) {
		String dbPassword = QueryHandler.getPassword(username);
		if (password != null && password.equals(dbPassword))
			return true;
		
		return false;
	}
	
	public String queryGenerator() {
		switch (query.getCommand()) {
			case "LOGIN":
				if (checkAuth(query.getUsername(), query.getPassword())) { // If the username and password are the same as in the DB
					isAuthenticated = true;
					return "OK - User Authenticated";
				}
				else // The username or processor are bad
					return "NO - Login failure: Invalid username or password";
			case "LIST":
				if (isAuthenticated && query.getUsername() != null) {
					String refRegex = "LIST\\s[a-zA-Z0-9@_.]+";
					String mailboxRegex = "LIST\\s[a-zA-Z0-9@_.]+\\s[a-zA-Z0-9_]+";
					String command = query.getFullCommand();
					ArrayList<String> resp = null;
					if (command.matches(refRegex)) {
						String userName = command.split("\\s")[1];
						resp = QueryHandler.listRef(userName);
					}
					else if (command.matches(mailboxRegex)) {
						String userName = command.split("\\s")[1];
						String mailboxName = command.split("\\s")[2];
						resp = QueryHandler.listMailbox(userName, mailboxName);
					}
					
					if (resp != null && !resp.isEmpty())
						return "OK - List Completed\n" + String.join("\n", resp);
					else
						return "NO - List Failure: Can't list that reference or name";
				}
			case "FETCH":
				String req = query.getFullCommand();
				ArrayList<String> reqParts = new ArrayList<String>(Arrays.asList(req.split("\\s")));
				if(reqParts.size() == 3) {
					String resp = "NO - fetch error: can't fetch that data";
					String emailID = reqParts.get(1);
					String respType = reqParts.get(2);
					HashMap<String, String> fetchData;
					String okString = "OK - fetch completed";
					switch (respType) {
						case "HEADER":
							ArrayList<String> respParts = new ArrayList<String>();
							fetchData = QueryHandler.fetch(emailID, respType);
							if (fetchData == null) return resp;
							boolean missingData = false;
							respParts.add(okString);
							for (String key : new String[] {"date", "to", "from", "subject"})
								if (fetchData.containsKey(key)) respParts.add(String.format("%s: %s", key.toUpperCase(), fetchData.get(key)));
								else missingData = true;
							if (!missingData) {
								resp = String.join("\n", respParts);
							}
							return resp;
						case "BODY":
							fetchData = QueryHandler.fetch(emailID, respType);
							if (fetchData != null && fetchData.containsKey("body")) {
								resp = String.join("\n", new String[] {okString, fetchData.get("body")});
							}
							return resp;
						case "FLAGS":
							//A bit redundant, I know...
							fetchData = QueryHandler.fetch(emailID, respType);
							if (fetchData != null && fetchData.containsKey("flags")) {
								resp = String.join("\n", new String[] {okString, fetchData.get("flags")});
							}
							return resp;
					}
				}
			default:
				return "BAD - Invalid or unknown command"; //TODO find out what ???? means || connection identifier? (if so we dont care)
		}
	}
}
