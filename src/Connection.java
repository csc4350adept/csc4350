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
					//grows data until it's big enough to accept more bytes
					while (pos + cur >= data.length) {
						byte[] tempData = new byte[data.length + chunkSize];
						System.arraycopy(data, 0, tempData, 0, data.length);
						data = tempData;
					}
					//Now fills data with the new buffer
					System.arraycopy(buffer, 0, data, pos, cur + 1);
					pos += cur;
					byte lastBufferByte = buffer[buffer.length - 1];
					//Kicks out of the loop if the last byte is a buffer or null byte
					//This can possibly break an SMTP command if the buffer ends right on the null byte separate in authentication TODO
					if (lastBufferByte == 10 || lastBufferByte == 0) {
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
						System.out.println("Error Writing resp to socket.");
						System.out.println(e.getMessage());
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
		//System.out.println("Connection closed.");
		//Close the connection here
	}
	
	abstract String handleResult(byte[] data);
	
}
