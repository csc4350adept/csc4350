//By B. Daniel Garber and Ed Bull

import java.util.ArrayList;
import java.util.HashMap;

public class SMTPProcessor extends CmdProcessor{

	// Byte Processing Variables
	private String cmd;
	
	// Prediction (Basic) Algorithm Variables
	private boolean prediction;
	private String expected;
	
	// Other variables
	private String emailAddress;
	private String date, to, from, subject, body = null;
		
	// Global Instances
	Query query;
	QueryHandler queryHandler;
		
	public SMTPProcessor() {
		query = new Query();
		System.out.println("Generated query from SMTPProcessor");
	}
		
	public String processBytes(byte[] command) {
		cmd = new String(processByteArray(command)); // For example "MAIL FROM:<email>" or RCPT TO:<email>"
		query.setCommand(cmd);
		
		return queryGenerator();
	}
	
	public void setEmailContent() {
		
		String _354 = "354 Send message content; end with <CRLF>.<CRLF>";
		System.out.println(_354);
		String data = query.getCommand().replaceAll("\r", "");
		
		if (data.toLowerCase().contains("date")) {
			
			date = data.substring(0, data.indexOf("\n"));
			data = data.replace("\n", "theToPart");
			
			to = data.substring(data.indexOf("theToPart"), data.indexOf("\n"));
			to = to.replace("theToPart", "");
			data = data.replace("\n", "theFromPart");
			
			from = data.substring(data.indexOf("theFromPart"), data.indexOf("\n"));
			from = from.replace("theFromPart", "");
			data = data.replace("\n", "theSubjectPart");
			
			subject = data.substring(data.indexOf("theSubjectPart"), data.indexOf("\n"));
			subject = subject.replace("theSubjectPart", "");
			data = data.replace("\n", "theBodyPart");
			
			body = data.substring(data.indexOf("theBodyPart"), data.indexOf("\n"));
			body = body.replace("theBodyPart", "");
		} else {
			System.out.println("Appropriate error message.");
		}
		
		if (body != null) {
			System.out.println("250 OK");
		} else {
			System.out.println("Appropriate error message.");
		}
	}
	
	public HashMap parseToHashMap (String date, String to, String from, String subject, String body) {
		HashMap<String, String> email = new HashMap<String, String>();
		
		email.put("date", this.date);
		email.put("to", this.to);
		email.put("from", this.from);
		email.put("subject", this.subject);
		email.put("body", this.body);
		
		return email;
	}
	
	public boolean checkAuth(String username, String password) {
		String dbPassword = QueryHandler.getPassword(username);
		if (password != null && password.equals(dbPassword))
			return true;
		
		return false;
	}
	
	public String queryGenerator() {
		if (checkAuth(query.getUsername(), query.getPassword())) { // If the username and password are the same as in the DB
			isAuthenticated = true;

			if (query.getCommand().contains("MAIL TO:")) {
				expected = "RCPT TO: " + emailAddress;
			} else if (query.getCommand().equals(expected) && !expected.equals("DATA")) {
				prediction = true;
				expected = "DATA";
			} else if (query.getCommand().equals(expected)) {
				prediction = true;
				setEmailContent();
				parseToHashMap(date, to, from, subject, body);
			} else {
				return "We were authenticated, but went wrong somewhere.";
			}
		} else {
			return "NO - Login failure: Invalid username or password";
		}
		return "Successfully executed SMTP queryGenerator()";
	}
}