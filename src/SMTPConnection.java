import javax.net.ssl.SSLSocket;

public class SMTPConnection extends Connection {
	SmtpProcessor processor = new SmtpProcessor();

	public SMTPConnection(SSLSocket connection) {
		super(connection);
	}

	@Override
	String handleResult(byte[] command) {
		return processor.processBytes(command);
	}
	
}
