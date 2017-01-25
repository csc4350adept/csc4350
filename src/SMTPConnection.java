import javax.net.ssl.SSLSocket;

public class SMTPConnection implements Runnable {
	private SSLSocket connection;
	
	public SMTPConnection(SSLSocket connection) {
		this.connection = connection;
		
	}

	@Override
	public void run() {
		System.out.println("Connection started");
	}
}
