
public class ServerController {

	public static void main(String[] args) {
		int smtpDefaultPort = 465;
		int imapDefaultPort = 993;
		//TODO
			//These should get pulled in order of priority from:
			//args, config file, defaults
			//eventually...
		int smtpPort = smtpDefaultPort;
		int imapPort = imapDefaultPort;
		
		Thread smtpServerThread = createServerThread("smtp", smtpPort);
		Thread imapServerThread = createServerThread("imap", imapPort);
		//TODO
		//Should catch the IllegalArgumentException
		//and do... something?
		smtpServerThread.start();
		imapServerThread.start();
		
		//Restarts server threads if they break
		do {
			if (smtpServerThread.getState() == Thread.State.TERMINATED) {
				smtpServerThread = createServerThread("smtp", smtpPort);
				smtpServerThread.start();
			}
			
			if (imapServerThread.getState() == Thread.State.TERMINATED) {
				imapServerThread = createServerThread("imap", imapPort);
				imapServerThread.start();
			}
		//TODO create a separate interactive thread that can take a quit command
			//Then check for that state here.
			//For now we just won't have a way to quit other than ctrl-c
		} while(true);
	}
	

	private static Thread createServerThread(String type, int port) throws IllegalArgumentException {
		Thread newThread;
		if (type.equals("smtp")) {
			newThread = new Thread(new Runnable() {
				public void run() {
					SmtpServer smtpServer = new SmtpServer(port);
					smtpServer.startServer();
				}
			});
		} else if (type.equals("imap")) {
			newThread = new Thread(new Runnable() {
				public void run() {
					ImapServer imapServer = new ImapServer(port);
					imapServer.startServer();
				}
			});
		} else {
			throw new IllegalArgumentException();
		}
		return newThread;
	}

}
