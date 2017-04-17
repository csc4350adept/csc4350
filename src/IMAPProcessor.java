import java.util.ArrayList;

//By B. Daniel Garber

public class IMAPProcessor extends CmdProcessor {
	// Byte Processing Variables
	private String cmd;
	
	// Global Instances
	Query query;
	QueryHandler queryHandler;
	
	public IMAPProcessor() {
		query = new Query();
		System.out.println("created queryyyyy");
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
	
	@SuppressWarnings("unchecked")
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
					System.out.println("AUTHED AND QUERIED MOFO");
					String refRegex = "LIST\\s[a-zA-Z0-9]+";
					String mailboxRegex = "LIST\\s[a-zA-Z0-9]+\\s[a-zA-Z0-9_]+";
					String command = query.getFullCommand();
					ArrayList<String> resp = null;
					System.out.println("command is " + command);
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
				return ""; //TODO
			default:
				return "BAD - Invalid or unknown command."; //TODO find out what ???? means || connection identifier? (if so we dont care)
		}
	}
}
