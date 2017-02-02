import javax.net.ssl.SSLSocket;

public class SMTPConnection extends Connection {

	public SMTPConnection(SSLSocket connection) {
		super(connection);
	}

	@Override
	String handleResult(byte[] data) {
		String resp;
		
		/*Debugging: right now this just echoes back what it received in both byte and string form*/
		resp = new String(data);
		System.out.println(resp);
		
		return resp;
	}
	
}
