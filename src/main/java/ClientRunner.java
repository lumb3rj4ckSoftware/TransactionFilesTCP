import Client.CopyFromClientToServer;
import Client.IsOnlineClient;

public class ClientRunner {
	public static void main(String[] args) {
		IsOnlineClient isOnlineClient = new IsOnlineClient("localhost", 6788);
//		IsOnlineClient isOnlineClient = new IsOnlineClient("localhost", 5000);
//		IsOnlineClient isOnlineClient = new IsOnlineClient("193.31.9.4", 5000);
//		IsOnlineClient isOnlineClient = new IsOnlineClient("193.31.9.4", 6788);
		System.out.println(isOnlineClient.checkIfOnline());

		CopyFromClientToServer copyFromClientToServer =  new CopyFromClientToServer("localhost", 6789);
//		CopyFromClientToServer copyFromClientToServer =  new CopyFromClientToServer("localhost", 5001);
//		CopyFromClientToServer copyFromClientToServer =  new CopyFromClientToServer("193.31.9.4", 5001);
//		CopyFromClientToServer copyFromClientToServer =  new CopyFromClientToServer("193.31.9.4", 6789);
		copyFromClientToServer.setSession(CopyFromClientToServer.generateNewSession());
		copyFromClientToServer.cpy("/home/lumb3rj4ck/Tmp/TestFiles");
	}
}
