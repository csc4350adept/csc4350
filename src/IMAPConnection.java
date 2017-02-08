import javax.net.ssl.SSLSocket;

public class IMAPConnection extends Connection {

	public IMAPConnection(SSLSocket connection) {
		super(connection);
	}

	@Override
	String handleResult(byte[] data) {
		String resp;
		
		/*Debugging: right now this just echoes back what it received in both byte and string form*/
		resp = new String(data);
		System.out.println("Received: " + resp + " : End of response.");
		
		return resp;
	}
	
}
