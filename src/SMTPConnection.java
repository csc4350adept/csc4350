import javax.net.ssl.SSLSocket;

public class SMTPConnection extends Connection {
	SMTPProcessor processor = new SMTPProcessor();

	public SMTPConnection(SSLSocket connection) {
		super(connection);
	}

	@Override
	String handleResult(byte[] command) {
		return processor.processBytes(command);
	}
	
}
