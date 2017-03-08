//By B. Daniel Garber

public class IMAPProcessor extends CmdProcessor {
	// Byte Processing Variables
	private String cmd;
	
	// Global Instances
	Query query;
	
	public IMAPProcessor() {

	}
	
	public String processBytes(byte[] command) {
		cmd = new String(processByteArray(command)); // For example "LOG USERNAME PASSWORD" format
		query = new Query(cmd);
		query.setCommand();
		
		return queryGenerator();
	}
	
	public boolean checkAuth(String username, String password) {
		String dbPassword = QueryHandler.getPassword(username);
		System.out.println(password);
		System.out.println(dbPassword);
		if (password != null && password.equals(dbPassword))
			return true;
		
		return false;
	}
	
	public String queryGenerator() {
		switch (query.getCommand()) {
			case "LOGIN":
				if (checkAuth(query.getUsername(), query.getPassword())) { // If the username and password are the same as in the DB
					isAuthenticated = true;
					return "???? OK - User Authenticated";
				}
				else // The username or processor are bad
					return "???? NO - Login failure: Invalid username or password";
			case "LIST":
				return "???? "; //TODO
			case "FETCH":
				return "???? "; //TODO
			default:
				return "???? BAD - Invalid or unknown command."; //TODO find out what ???? means || connection identifier? (if so we dont care)
		}
	}
}
