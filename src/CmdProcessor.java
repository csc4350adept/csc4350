//By B. Daniel Garber

// Process byte array and return it in string form
// Return the response the server will send back to the client
public class CmdProcessor {
	public boolean isAuthenticated; // false by default
	
	public String processByteArray(byte[] byteArray) {
		return new String (byteArray);
	}
	
}