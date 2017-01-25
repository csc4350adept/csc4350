import java.security.KeyStore;
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
			context.init(keyFactory.getKeyManagers(), null,  null);
			SSLServerSocketFactory socketFactory = context.getServerSocketFactory();
			this.server = (SSLServerSocket) socketFactory.createServerSocket(this.port);		
		} catch (Exception e) {
			System.err.println(e.toString());
		}	
	}
	
	public void startServer() {
		//Start listening for a connection
		//If the connection starts, do stuff
		while (true) {
			try {
				SSLSocket connection = (SSLSocket) this.server.accept();
				Thread minion = createConnectionThread(connection);
				minion.start();
			} catch (Exception e) {
				//If this catch is tripping, check to be sure that the keyFile exists in the correct path (same path as executing file)
				System.err.println(e.toString());
			}
		}
	}
	
	abstract int setServerPort();
	abstract Thread createConnectionThread(SSLSocket connection);
}
