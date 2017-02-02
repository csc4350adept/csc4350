import javax.net.ssl.SSLSocket;

public class SmtpServer extends SSLServer{
	@Override
	int setServerPort() {
		return ServerController.getSmtpPort();
	}

	@Override
	Thread createConnectionThread(SSLSocket socket) {
		return new Thread(new Runnable() {
			public void run() {
				SMTPConnection connection = new SMTPConnection(socket);
				connection.start();
			}
		});
	}	
	
}
