import javax.net.ssl.SSLSocket;

public class IMAPConnection implements Runnable {
	private SSLSocket connection;
	
	public IMAPConnection(SSLSocket connection) {
		this.connection = connection;
		
	}

	@Override
	public void run() {
		System.out.println("Connection started");
	}
}
