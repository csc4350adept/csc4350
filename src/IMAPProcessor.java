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
		String req;
		ArrayList<String> reqParts;
		String resp = "BAD - Invalid or unknown command";
		
		if (query.getCommand() == null) return resp;
		
		switch (query.getCommand()) {
			case "LOGIN":
				if (checkAuth(query.getUsername(), query.getPassword())) { // If the username and password are the same as in the DB
					isAuthenticated = true;
					resp = "OK - User Authenticated";
				}
				else // The username or processor are bad
					resp = "NO - Login failure: Invalid username or password";
				break;
			
			case "LIST":
				if (isAuthenticated && query.getUsername() != null) {
					String refRegex = "LIST\\s[a-zA-Z0-9@_.]+";
					String mailboxRegex = "LIST\\s[a-zA-Z0-9@_.]+\\s[a-zA-Z0-9_]+";
					String command = query.getFullCommand();
					ArrayList<String> result = null;
					if (command.matches(refRegex)) {
						String userName = command.split("\\s")[1];
						result = QueryHandler.listRef(userName);
					}
					else if (command.matches(mailboxRegex)) {
						String userName = command.split("\\s")[1];
						String mailboxName = command.split("\\s")[2];
						result = QueryHandler.listMailbox(userName, mailboxName);
					}
					
					if (result != null)
						resp = "OK - List Completed\n" + String.join("\n", result);
					else
						resp = "NO - List Failure: Can't list that reference or name";
				}
				break;
				
			case "FETCH":
				if (!isAuthenticated || query.getUsername() == null) break;
				req = query.getFullCommand();
				reqParts = new ArrayList<String>(Arrays.asList(req.split("\\s")));
				if(reqParts.size() == 3) {
					resp = "NO - fetch error: can't fetch that data";
					String emailID = reqParts.get(1);
					String respType = reqParts.get(2);
					HashMap<String, String> fetchData;
					String okString = "OK - fetch completed";
					switch (respType) {
						case "BODY[HEADER]":
							ArrayList<String> respParts = new ArrayList<String>();
							fetchData = QueryHandler.fetch(emailID, respType);
							if (fetchData == null) break;
							boolean missingData = false;
							respParts.add(okString);
							for (String key : new String[] {"date", "to", "from", "subject"}) {
								if (fetchData.containsKey(key)) respParts.add(String.format("%s: %s", key.toUpperCase(), fetchData.get(key)));
								else missingData = true;
							}
							if (!missingData) {
								resp = String.join("\n", respParts);
							}
							break;
						case "BODY[TEXT]":
							fetchData = QueryHandler.fetch(emailID, respType);
							if (fetchData != null && fetchData.containsKey("body")) {
								resp = String.join("\n", new String[] {okString, fetchData.get("body")});
							}
							break;
						case "FLAGS":
							//A bit redundant, I know...
							fetchData = QueryHandler.fetch(emailID, respType);
							if (fetchData != null && fetchData.containsKey("read")) {
								resp = String.join("\n", new String[] {okString, fetchData.get("read")});
							}
							break;
					}
				}
				break;
			
			case "APPEND":
				if (!isAuthenticated || query.getUsername() == null) break;
				resp = "NO - append error: can't append to that mailbox, error in flags or date/time or message text";
				req = query.getFullCommand();
				reqParts = new ArrayList<String>(Arrays.asList(req.split("\\s")));
				if (req.split("\\s").length == 3) {
					String appendID = reqParts.get(1);
					String appendFlag = reqParts.get(2).toLowerCase();
					if (appendFlag.startsWith("\\") && appendFlag.length() > 1) appendFlag = appendFlag.substring(1);
					switch (appendFlag) {
						case "read":
							if (QueryHandler.setRead(appendID)) resp = "OK - append completed";
							break;
						default:
							resp = "NO - append error: can't append to that mailbox, error in flags";
					}
				}
				break;
			
			//not tested
			case "CREATE":
				if (!isAuthenticated || query.getUsername() == null) break;
				resp = "BAD - command unknown or arguments invalid";
				req = query.getFullCommand();
				reqParts = new ArrayList<String>(Arrays.asList(req.split("\\s")));
				if (reqParts.size() == 2) {
					resp = "NO - create failure: can't create mailbox with that name";
					String mailbox = reqParts.get(1);
					if (mailbox.matches("\"[^\"]+\"")) mailbox = mailbox.replaceAll("\"", "");
					else break;
					if (QueryHandler.createMailbox(query.getUsername(), mailbox)) resp = "OK - create completed";
				}
				break;
			
			//not tested
			case "DELETE":
				if (!isAuthenticated || query.getUsername() == null) break;
				resp = "BAD - command unknown or arguments invalid";
				req = query.getFullCommand();
				reqParts = new ArrayList<String>(Arrays.asList(req.split("\\s")));
				if (reqParts.size() == 2) {
					resp = "NO - delete failure: can't delete mailbox with that name";
					String mailbox = reqParts.get(1);
					if (mailbox.matches("\"[^\"]+\"")) mailbox = mailbox.replaceAll("\"", "");
					else break;
					if (QueryHandler.deleteMailbox(query.getUsername(), mailbox)) resp = "OK - delete completed";
				}
				break;
				
			//not tested
			case "RENAME":
				if (!isAuthenticated || query.getUsername() == null) break;
				resp = "BAD - command unknown or arguments invalid";
				req = query.getFullCommand();
				reqParts = new ArrayList<String>(Arrays.asList(req.split("\\s")));
				if (reqParts.size() == 3) {
					resp = "NO - rename failure: can't rename mailbox with that name, can't rename to mailbox with that name";
					String mailbox = reqParts.get(1);
					if (mailbox.matches("\"[^\"]+\"")) mailbox = mailbox.replaceAll("\"", "");
					else break;
					String newName = reqParts.get(2);
					if (newName.matches("\"[^\"]+\"")) newName = newName.replaceAll("\"", "");
					else break;
					if (QueryHandler.renameMailbox(query.getUsername(), mailbox, newName)) resp = "OK - rename completed";
				}
				break;
				
			//not tested
			case "UID":
				if (!isAuthenticated || query.getUsername() == null) break;
				resp = "BAD - command unknown or arguments invalid";
				req = query.getFullCommand();
				reqParts = new ArrayList<String>(Arrays.asList(req.split("\\s")));
				if (reqParts.size() >= 4) {
					if (!reqParts.get(1).equals("MOVE")) break;
					resp = "NO - move error: can't move those messages or to that name";
					String email = reqParts.get(2);
					if (!email.matches("[0-9]+")) break;
					String mailbox = String.join(" ", reqParts.subList(3, reqParts.size()));
					if (QueryHandler.moveEmail(query.getUsername(), email, mailbox)) resp = "OK - move completed";
				} else if (reqParts.size() == 3) {
					if (!reqParts.get(1).equals("EXPUNGE")) break;
					resp = "NO - expunge error: can't expunge those messages";
					String email = reqParts.get(2);
					if (!email.matches("[0-9]+")) break;
					if (QueryHandler.deleteEmail(email)) resp = "OK - expunge completed";
				}
				break;
				
			default:
				resp = "BAD - Invalid or unknown command";
		}
		return resp;
	}
}
