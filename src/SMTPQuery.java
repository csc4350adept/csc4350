import java.util.ArrayList;
import java.util.Arrays;

/* SMTP was very, very broken. I don't have time to do proper inheritance, so I'm just copying Daniel's Query class and modifying it for SMTP */

public class SMTPQuery {
	// GLOBAL Variables
	private String fullCommand;
	private String command;
	
	// LOGIN Variables
	private String username;
	private String password;
	
	// UPDATE Variables
	private String mailbox;
	
	// GLOBAL Instances
	
	public SMTPQuery () {
		
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
		else if (commandParts.get(0).equals("MAIL")) {
			
			return command = "MAIL";
		}
		else if (commandParts.get(0).contains("RCPT")) {
			return command = "RCPT";
		}
		else if (commandParts.get(0).contains("DATA")) {
			return command = "DATA";
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
