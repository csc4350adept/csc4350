/*
 * There are no outputs yet, I'll put those in tomorrow.
 * You can still connect and see a "Connection started" message in System.out
 * I use openssl s_client -connect localhost:465 -tls1
 * Right now there is no certificate trust, only self-signed
 * No diffie hellman perfect forward secrecy
 * Probably should limit ciphers and do tls 1.2 only
 */


public class ServerController {
	private static int smtpPort;
	private static int imapPort;
	private static String hostname;
	private static String keysFilePath;
	private static char[] keysFilePwd;
	private static char[] keysPwd;
	private static int smtpMaxSize;

	public static void main(String[] args) {
		//Note, args not like python
		//args[0] is the first arg, not the command string
		int smtpDefaultPort = 465;
		int imapDefaultPort = 993;
		String defaultHostname = "localhost";
		//TODO
			//These should get pulled in order of priority from:
			//args, config file, defaults
			//eventually...
		smtpPort = smtpDefaultPort;
		imapPort = imapDefaultPort;
		hostname = defaultHostname;
		keysFilePath = "sekeys.jks";
		keysFilePwd = "foobar".toCharArray();
		keysPwd = "123foobar!".toCharArray();
		smtpMaxSize = 250000000;
		
		
		Thread smtpServerThread = createServerThread("smtp", smtpPort);
		Thread imapServerThread = createServerThread("imap", imapPort);
		//TODO
		//Should catch the IllegalArgumentException
		//and do... something?
		System.out.println("Server starting");
		smtpServerThread.start();
		imapServerThread.start();
		
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
					SmtpServer smtpServer = new SmtpServer();
					smtpServer.startServer();
				}
			});
		} else if (type.equals("imap")) {
			newThread = new Thread(new Runnable() {
				public void run() {
					ImapServer imapServer = new ImapServer();
					imapServer.startServer();
				}
			});
		} else {
			throw new IllegalArgumentException();
		}
		return newThread;
	}
	
	public static String getHostname() {
		return hostname;
	}
	public static int getSmtpPort() {
		return smtpPort;
	}
	public static int getImapPort() {
		return imapPort;
	}
	public static String getKeysFilePath() {
		return keysFilePath;
	}
	public static char[] getKeysFilePwd() {
		return keysFilePwd;
	}
	public static char[] getKeysPwd() {
		return keysPwd;
	}
	public static int getSmtpMaxSize() {
		return smtpMaxSize;
	}
}
