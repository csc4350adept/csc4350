import java.io.*;
import java.net.SocketException;

import javax.net.ssl.SSLSocket;

abstract public class Connection {
	private SSLSocket connection;
	
	public Connection(SSLSocket connection) {
		this.connection = connection;
	}
	
	public void start() {
		System.out.println("Connection started.");
		while (true) {
			byte[] data = new byte[0];
			String resp;
			int chunkSize = 64;
			byte[] buffer = new byte[chunkSize];
			try {
				InputStream in = this.connection.getInputStream();
				OutputStream out = this.connection.getOutputStream();
				int cur;
				int pos = 0;
				//Read in bytes into buffer and compile them into result
				//Break on newlines
				while ((cur = in.read(buffer)) > 0) {
					while (pos + cur > data.length) {
						byte[] tempData = new byte[data.length + chunkSize];
						System.arraycopy(data, 0, tempData, 0, data.length);
						data = tempData;
					}
					System.arraycopy(buffer, 0, data, pos, cur); //it's losing the final character here
					pos += cur;
					byte lastBufferByte = buffer[buffer.length - 1];
					if (lastBufferByte == 10) {
						break;
					}
					buffer = new byte[chunkSize];
				}
				byte[] result = new byte[pos];
				System.arraycopy(data, 0, result, 0, pos);
				
				//If a result is present, send it to be handled
				//Break the listen cycle if the socket is unresponsive to writes or there's no incoming data
				if (result.length > 0) {
					resp = handleResult(result);
					try {
						out.write(resp.getBytes());
						out.flush();
					} catch (SocketException e) {
						break;
					}
				} else {
					out.flush();
					break;
				}
			} catch (IOException e) {
				System.out.println(e.toString());
				return;
			}
		}
		System.out.println("Connection closed.");
		//Close the connection here
	}
	
	abstract String handleResult(byte[] data);
	
}
