package Client;

import file.fileStructure.PathUtils;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class IsOnlineClient {

	private final static int DEFAULT_SIZE_FILE_DESCRIPTION = 2048;
	private final static int DEFAULT_CHUNK_SIZE = 2048;

	private final static String IS_ONLINE = "ISONLINE?";
	private final static String YES = "YES";

	private Socket clientSocket;
	private DataOutputStream outToServer;
	private BufferedReader inFromServer;

	public IsOnlineClient(String ip, int port) {
		try {
			clientSocket = new Socket(ip, port);
		} catch (IOException e) {
			System.out.println(e);
			throw new IllegalStateException("clientSocket could not be established");
		}

		try {
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			throw new IllegalStateException("streams could not be assigned");
		}
	}


	public boolean checkIfOnline() {
		try {
			outToServer.write(IS_ONLINE.getBytes());
			String ack = inFromServer.readLine();
			if(ack.equalsIgnoreCase(YES)) {
				return true;
			}

			return false;

		} catch (IOException e) {
			System.out.println("Could not check if server is online");
		}

		return false;
	}
}
