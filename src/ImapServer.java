import javax.net.ssl.SSLSocket;

public class ImapServer extends SSLServer{
	@Override
	int setServerPort() {
		return ServerController.getImapPort();
	}

	@Override
	Thread createConnectionThread(SSLSocket connection) {
		return new Thread(new IMAPConnection(connection), "IMAPConnection");
	}	
	
}
