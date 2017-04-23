import java.util.ArrayList;
import java.util.Arrays;

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
		ArrayList<String> commandParts = new ArrayList<String>(Arrays.asList(command.split("\\s")));
		if (commandParts.get(0).equals("LOGIN")) {
			String[] loginArray = command.split(" ");
			
			if (loginArray.length == 3) { // Precisely "LOGIN Username Password" format
				username = loginArray[1].trim();
				password = loginArray[2].trim();
				return command = "LOGIN";
			}
		}
		else if (commandParts.get(0).equals("LIST")) {
			
			return command = "LIST";
		}
		else if (commandParts.get(0).contains("FETCH")) {
			return command = "FETCH";
		}
		else if (commandParts.get(0).contains("APPEND")) {
			return command = "APPEND";
		}
		else if (commandParts.get(0).contains("CREATE")) {
			return command = "CREATE";
		}
		else if (commandParts.get(0).contains("DELETE")) {
			return command = "DELETE";
		}
		else if (commandParts.get(0).contains("RENAME")) {
			return command = "RENAME";
		}
		else if (commandParts.get(0).contains("UID")) {
			return command = "UID";
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
