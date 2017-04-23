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
					while (pos + cur > data.length) {
						byte[] tempData = new byte[data.length + chunkSize];
						System.arraycopy(data, 0, tempData, 0, data.length);
						data = tempData;
					}
					//Now fills data with the new buffer
					System.arraycopy(buffer, 0, data, pos, cur);
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
						//Need to add a null byte to the end of the response string
						byte [] tmp = resp.getBytes();
						byte[] byteResp = new byte[tmp.length + 1];
						java.lang.System.arraycopy(tmp, 0, byteResp, 0, tmp.length);
						byteResp[byteResp.length - 1] = 0;
						out.write(byteResp);
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
			} catch (SocketException e) {
				if (e.getMessage().equals("Connection reset")) {
					System.out.println(e.getMessage());
					try {
						connection.close();
					} catch (IOException IOe) {
						/*Do nothing*/
					}
					return;
				}
			}catch (IOException e) {
				System.out.println(e.toString());
				return;
			}
		}
		//System.out.println("Connection closed.");
		//Close the connection here
	}
	
	abstract String handleResult(byte[] data);
	
}
