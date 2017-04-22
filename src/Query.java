//By B. Daniel Garber

public class Query {
	// GLOBAL Variables
	private String fullCommand;
	private String command;
	
	// LOGIN Variables
	private String username;
	private String password;
	
	// UPDATE Variables
	private String mailbox;
	
	// GLOBAL Instances
	
	public Query () {
		
	}
	
	public String setCommand(String cmd) {
		fullCommand = cmd;
		command = cmd;
		if (command.substring(0, command.indexOf(" ")).equals("LOGIN")) {
			String[] loginArray = command.split(" ");
			
			if (loginArray.length == 3) { // Precisely "LOGIN Username Password" format
				username = loginArray[1].trim();
				password = loginArray[2].trim();
				return command = "LOGIN";
			}
		}
		else if (command.substring(0, command.indexOf(" ")).equals("LIST")) {
			
			return command = "LIST";
		}
		else if (command.substring(0, command.indexOf(" ")).contains("FETCH")) {
			return command = "FETCH";
		}
		else if (command.substring(0, command.indexOf(" ")).contains("APPEND")) {
			return command = "APPEND";
		}
		
		return null;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getCommand() {
		return command;
	}
	
	public String getMailbox() {
		return mailbox;
	}
	
	public String getFullCommand() {
		return fullCommand;
	}
}
