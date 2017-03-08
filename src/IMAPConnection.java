import javax.net.ssl.SSLSocket;

public class IMAPConnection extends Connection {
	IMAPProcessor processor = new IMAPProcessor();

	public IMAPConnection(SSLSocket connection) {
		super(connection);
	}

	@Override
	String handleResult(byte[] command) {
		/*
		 * Debug stuff
		 */
		System.out.println("Received command: " + new String(command));
		String resp = processor.processBytes(command);
		System.out.println("Generated response: " + resp);
		return resp;
	}
	
}
