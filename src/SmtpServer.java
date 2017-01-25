
import javax.net.ssl.SSLSocket;

public class SmtpServer extends SSLServer{
	@Override
	int setServerPort() {
		return ServerController.getSmtpPort();
	}

	@Override
	Thread createConnectionThread(SSLSocket connection) {
		return new Thread(new SMTPConnection(connection), "SMTPConnection");
	}	
	
}
