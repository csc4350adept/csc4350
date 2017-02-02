import javax.net.ssl.SSLSocket;

public class ImapServer extends SSLServer{
	@Override
	int setServerPort() {
		return ServerController.getImapPort();
	}

	@Override
	Thread createConnectionThread(SSLSocket socket) {
		return new Thread(new Runnable() {
			public void run() {
				IMAPConnection connection = new IMAPConnection(socket);
				connection.start();
			}
		});
	}	
	
}
