//By B. Daniel Garber and Ed Bull

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class SMTPProcessor extends CmdProcessor{

	// Byte Processing Variables
	private String cmd;
	
	// Prediction (Basic) Algorithm Variables
	private boolean prediction;
	private String expected;
	
	// Other variables
	private ArrayList<String> rcpts = new ArrayList<String>();
	private String emailAddress = null;
	private String date, to, from, subject, body = null;
		
	// Global Instances
	SMTPQuery query;
	QueryHandler queryHandler;
		
	public SMTPProcessor() {
		query = new SMTPQuery();
		System.out.println("Generated query from SMTPProcessor");
	}
		
	public String processBytes(byte[] command) {
		cmd = new String(processByteArray(command)); // For example "MAIL FROM:<email>" or RCPT TO:<email>"
		query.setCommand(cmd);
		return queryGenerator();
	}
	
	public String setEmailContent(String message) {
		String resp = "501 Bad format";
		message = message.replaceAll("\r", ""); //because fuck Microsoft and their advocacy of the CRLF standard
		
		to = String.join(", ", rcpts);
		from = emailAddress;
		
		String dateRegex = "date:\\s[^\\s].*";
		String toRegex = "to:\\s[^\\s].*";
		String fromRegex = "from:\\s[^\\s].*";
		String subjectRegex = "subject:\\s[^\\s].*";
		
		
		//Get the headers and remove them from the message
		//Don't store to and from because we already set those and verified them with the MAIL TO: and RCPT TO: messages
		//We're not going to support address aliasing unless we have time to go for stretch goals
		
		ArrayList<String> lines = new ArrayList<String>(Arrays.asList(message.split("\n")));
		ArrayList<String> lineParts;
		ArrayList<String> tmp = new ArrayList<String>();
		
		for (String line : lines) {
			if(line.toLowerCase().matches(dateRegex)) {
				lineParts = new ArrayList<String>(Arrays.asList(line.split("\\s")));
				date = String.join(" ", lineParts.subList(1, lineParts.size() - 1));
				tmp.add(line);
			}
			if(line.toLowerCase().matches(subjectRegex)) {
				lineParts = new ArrayList<String>(Arrays.asList(line.split("\\s")));
				subject = String.join(" ", lineParts.subList(1, lineParts.size() - 1));
				tmp.add(line);
			}
			if(line.toLowerCase().matches(toRegex)) {
				tmp.add(line);
			}
			if(line.toLowerCase().matches(fromRegex)) {
				tmp.add(line);
			}
		}
		lines.removeAll(tmp);
		
		//verify we are left with a valid body message
		if (lines.size() < 2) return resp;
		if (!lines.get(0).equals("") && !lines.get(lines.size() - 1).equals(".")) return resp;
		body = String.join("\\n", lines.subList(1, lines.size() - 1)).trim();
		
		HashMap<String, String> email = parseToHashMap(date, to, from, subject, body);
		
		if (QueryHandler.receiveEmail(rcpts, email)) resp = "250 OK";
		else resp = "554 Transaction failed";
		
		return resp;
		/*This is causing crazy indexOutOfBoundsException problems
		 * Fixing it is taking forever, going to redo it
		 * 
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
		*/
	}
	
	public HashMap<String, String> parseToHashMap (String date, String to, String from, String subject, String body) {
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
		String req;
		String fullReq;
		ArrayList<String> reqParts;
		String resp = "500 Unrecognized command";
		
		System.out.println(query.getFullCommand());
		
		if (query.getCommand() == null) return resp;
		
		if (prediction && expected != null && !query.getCommand().matches(expected)) {
			return "503 Bad sequence";
		}
		
		if (prediction && expected == null) {
			return setEmailContent(query.getFullCommand());
		}
		
		switch (query.getCommand()) {
			case "LOGIN":
				//This is an internal mail system that requires authentication
				//We are using IMAP authentication here even though we're on SMTP
				//Mostly because it's just easier
				if (checkAuth(query.getUsername(), query.getPassword())) { // If the username and password are the same as in the DB
					isAuthenticated = true;
					System.out.println("Authenticated");
					resp = "OK - User Authenticated";
				}
				else // The username or processor are bad
					resp = "NO - Login failure: Invalid username or password";
				break;
			case "MAIL":
				if (!isAuthenticated) {
					resp = "503 Authentication required";
					break;
				}
				fullReq = query.getFullCommand();
				if (fullReq.split("\\s").length != 3) {
					resp = "501 Invalid arguments";
					break;
				}
				reqParts = new ArrayList<String>(Arrays.asList(fullReq.split("\\s")));
				if (!reqParts.get(1).equals("FROM:")) {
					resp = "501 Bad syntax";
					break;
				}
				if (!QueryHandler.emailExists(reqParts.get(2))) {
					resp = "550 Email does not exist";
					break;
				}
				emailAddress = reqParts.get(2);
				prediction = true;
				expected = "RCPT";
				resp = "250 OK";
				break;
			case "RCPT":
				if (!isAuthenticated) {
					resp = "503 Authentication required";
					break;
				}
				fullReq = query.getFullCommand();
				if (fullReq.split("\\s").length != 3) {
					resp = "501 Invalid arguments";
					break;
				}
				reqParts = new ArrayList<String>(Arrays.asList(fullReq.split("\\s")));
				if (!reqParts.get(1).equals("TO:")) {
					resp = "501 Bad syntax";
					break;
				}
				//Right now we only accept emails to the local server. We don't support the Send External use case yet
				if (!QueryHandler.emailExists(reqParts.get(2))) resp = "550 Email does not exist";
				rcpts.add(reqParts.get(2));
				prediction = true;
				expected = "(RCPT|DATA)";
				resp = "250 OK";
				break;
			case "DATA":
				if (!isAuthenticated) {
					resp = "503 Authentication required";
					break;
				}
				fullReq = query.getFullCommand();
				if (fullReq.split("\\s").length != 1) {
					resp = "501 Invalid arguments";
					break;
				}
				prediction = true;
				expected = null;
				resp = "354 Send message content; end with <CRLF>.<CRLF>";
				break;
		}
		/* I need to redo this
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
		*/
		System.out.println("Sending: " + resp);
		return resp;
		}
	
}