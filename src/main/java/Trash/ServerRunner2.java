package Trash;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import Trash.Server;

public class ServerRunner2 {

	public ServerRunner2() {};

	public void start() {
		String basePath = "/home/lumb3rj4ck/Tmp/ServerFiles";
		int port = 5001;

		try {
			ServerSocket serverSocket = new ServerSocket(port);
			while(true) {
				Socket socket = serverSocket.accept();
				Server server = new Server(socket, basePath);
				server.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
