import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

import java.io.*;

abstract public class SSLServer {
	private SSLServerSocket server;
	private int port;
	private String keysFilePath;
	private char[] keysFilePwd;
	private char[] keysPwd;
	
	public SSLServer() {
		this.port = setServerPort();
		this.keysFilePath = ServerController.getKeysFilePath();
		this.keysFilePwd = ServerController.getKeysFilePwd();
		this.keysPwd = ServerController.getKeysPwd();
		
		try {
			KeyStore keys = KeyStore.getInstance("JKS");
			File keysFile = new File(this.keysFilePath);
			if (keysFile.exists() && !keysFile.isDirectory()) {
				keys.load(new FileInputStream(this.keysFilePath), this.keysFilePwd);
			} else {
				//TODO Throw some kind of error here
				System.out.println("Incorrect keyFile");
				return;
			}
			//TODO Assuming a key file is loaded, check to see if there are keys in it
			//If not, create some and store them in it
			//Key creation should probably be done in a separate class
			
			//Load the keys and create the SSLServer
			KeyManagerFactory keyFactory = KeyManagerFactory.getInstance("SunX509");
			keyFactory.init(keys, this.keysPwd);
			SSLContext context = SSLContext.getInstance("TLS");
			
			//Create TrustManager
			//Server doesn't verify identity of clients at initial connection
			//Only clients verify the identity of the server
			TrustManager trustManager = new X509TrustManager() {

				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					//Be naive
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					//Be naive
					
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					//Be naive
					return null;
				}
			};	
			
			context.init(keyFactory.getKeyManagers(), new TrustManager[] {trustManager},  null);
			SSLServerSocketFactory socketFactory = context.getServerSocketFactory();
			this.server = (SSLServerSocket) socketFactory.createServerSocket(this.port);		
		} catch (java.net.SocketException e) {
			String eMsg = e.getMessage();
			switch (eMsg) {
				case "Connection reset":
					System.out.println("Connection reset");
			}
		} catch (Exception e) {
			System.err.println(e.toString());
		}	
	}
	
	public void startServer() {
		//Start listening for a connection
		//If the connection starts, do stuff
		boolean firstFailure = true;
		System.out.println("Server is listening on port " + port);
		while (true) {
			try {
				SSLSocket connection = (SSLSocket) this.server.accept();
				Thread minion = createConnectionThread(connection);
				minion.start();
				firstFailure = true;
			} catch (NullPointerException e) {
				if (firstFailure) {
					System.out.println(String.format("Server could not initialize. Check to see if port %d is open and available, or that the server program has appropriate permission to access it. A common cause of this issue is that another instance of the ADEPT server is already running.", port));
					firstFailure = false;
				}
			} catch (Exception e) {
				if (firstFailure) {
					System.out.println("Server could not initialize. Unknown error");
					firstFailure = false;
				}
			}
		}
	}
	
	abstract int setServerPort();
	abstract Thread createConnectionThread(SSLSocket connection);
}
