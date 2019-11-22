package Server;

import Server.ServerMaintainer.ServerHandler.ServerHandlerImpl;
import file.fileStructure.PathUtils;
import org.json.JSONObject;

import java.io.*;
import java.util.Arrays;

public class OnlineServerHandler extends ServerHandlerImpl {

	private final static int DEFAULT_SIZE_FILE_DESCRIPTION = 2048;
	private final static char NEW_LINE = '\n';
	private final static char NEW_RIGHT = '\r';

	private final static String IS_ONLINE = "ISONLINE?";
	private final static String YES = "YES";

	public OnlineServerHandler() {
		System.out.println("OnlineServerHandler started");
	}

	@Override
	public void action(InputStream inputStream, OutputStream outputStream) throws IOException {

		byte[] buffer = new byte[DEFAULT_SIZE_FILE_DESCRIPTION];
		int readBytes = inputStream.read(buffer, 0, buffer.length);
		buffer = Arrays.copyOfRange(buffer, 0, readBytes);

		if(!isOnline(buffer)) {
			System.out.println("Suspicious Request. Killing connection with client ");
			return;
		}
		System.out.println("ISONLINE Request came in...");

		//TODO: add something to verify if server is online or not
		String answer = YES + NEW_RIGHT + NEW_LINE;
//			answer = "NOPE" + NEW_RIGHT + NEW_LINE;
		outputStream.write(answer.getBytes());

		System.out.println("ISONLINE Request answered with yes ");
	}

	private boolean isOnline(byte[] buffer) {
		return new String(buffer).equals(IS_ONLINE);
	}
}